package org.sopt.makers.internal.community.service.post.crew;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.sopt.makers.internal.external.makers.CrewPost;
import org.sopt.makers.internal.external.makers.CrewPostListResponse;
import org.sopt.makers.internal.external.makers.MakersCrewClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CrewMeetingFeedCacheService {

	private static final String CACHE_PREFIX = "community:free-feed:meeting:";
	private static final Duration CACHE_TTL = Duration.ofMinutes(10);
	private static final int CREW_POST_CACHE_TAKE = 50;
	private static final int PREFETCH_BUFFER_COUNT = 20;

	private final MakersCrewClient makersCrewClient;
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	public CrewMeetingFeedCache getOrBuildCache(
		Long userId,
		LocalDateTime snapshotTime,
		int minimumRequiredCount
	) {
		return getOrBuildCache(
			userId,
			snapshotTime,
			minimumRequiredCount,
			PREFETCH_BUFFER_COUNT
		);
	}

	public CrewMeetingFeedCache getOrBuildPreviewCache(
		Long userId,
		LocalDateTime snapshotTime,
		int minimumRequiredCount
	) {
		return getOrBuildCache(
			userId,
			snapshotTime,
			minimumRequiredCount,
			0
		);
	}

	public CrewMeetingFeedCache getOrBuildPopularPreviewCache(
		Long userId,
		LocalDateTime snapshotTime,
		LocalDateTime since,
		int maxPageCount
	) {
		CrewMeetingFeedCache cache = readCache(userId, snapshotTime);

		if (containsOlderThanOrEqualToSince(cache.safePosts(), since) || !cache.hasMorePage()) {
			return cache;
		}

		CrewMeetingFeedCache rebuiltCache = rebuildCacheUntil(
			userId,
			snapshotTime,
			since,
			maxPageCount
		);

		writeCache(userId, snapshotTime, rebuiltCache);
		return rebuiltCache;
	}

	private CrewMeetingFeedCache rebuildCacheUntil(
		Long userId,
		LocalDateTime snapshotTime,
		LocalDateTime since,
		int maxPageCount
	) {
		int page = 1;
		boolean hasMorePage = true;
		List<CachedCrewPost> cachedPosts = new java.util.ArrayList<>();

		while (page <= maxPageCount && hasMorePage) {
			CrewPostListResponse crewResponse = makersCrewClient.getPosts(
				userId,
				page,
				CREW_POST_CACHE_TAKE
			);

			if (crewResponse == null || crewResponse.posts() == null) {
				hasMorePage = false;
				break;
			}

			boolean reachedOlderThanSince = false;

			for (CrewPost crewPost : crewResponse.posts()) {
				if (crewPost.createdDate().isAfter(snapshotTime)) {
					continue;
				}

				cachedPosts.add(CachedCrewPost.from(crewPost));

				if (crewPost.createdDate().isBefore(since)) {
					reachedOlderThanSince = true;
				}
			}

			hasMorePage = crewResponse.pageMeta() != null
				&& Boolean.TRUE.equals(crewResponse.pageMeta().hasNextPage());

			if (reachedOlderThanSince) {
				break;
			}

			page++;
		}

		return new CrewMeetingFeedCache(
			normalize(cachedPosts),
			hasMorePage
		);
	}

	private boolean containsOlderThanOrEqualToSince(
		List<CachedCrewPost> cachedPosts,
		LocalDateTime since
	) {
		return cachedPosts.stream()
			.anyMatch(post -> !post.createdDate().isAfter(since));
	}

	private CrewMeetingFeedCache getOrBuildCache(
		Long userId,
		LocalDateTime snapshotTime,
		int minimumRequiredCount,
		int prefetchBufferCount
	) {
		CrewMeetingFeedCache cache = readCache(userId, snapshotTime);

		if (cache.safePosts().size() >= minimumRequiredCount || !cache.hasMorePage()) {
			return cache;
		}

		int targetCount = minimumRequiredCount + prefetchBufferCount;
		CrewMeetingFeedCache rebuiltCache = rebuildCache(userId, snapshotTime, targetCount);

		writeCache(userId, snapshotTime, rebuiltCache);
		return rebuiltCache;
	}

	private CrewMeetingFeedCache rebuildCache(
		Long userId,
		LocalDateTime snapshotTime,
		int targetCount
	) {
		int page = 1;
		boolean hasMorePage = true;
		List<CachedCrewPost> cachedPosts = new java.util.ArrayList<>();

		while (cachedPosts.size() < targetCount && hasMorePage) {
			CrewPostListResponse crewResponse = makersCrewClient.getPosts(
				userId,
				page,
				CREW_POST_CACHE_TAKE
			);

			if (crewResponse == null || crewResponse.posts() == null) {
				hasMorePage = false;
				break;
			}

			for (CrewPost crewPost : crewResponse.posts()) {
				if (crewPost.createdDate().isAfter(snapshotTime)) {
					continue;
				}

				cachedPosts.add(CachedCrewPost.from(crewPost));
			}

			hasMorePage = crewResponse.pageMeta() != null
				&& Boolean.TRUE.equals(crewResponse.pageMeta().hasNextPage());

			page++;
		}

		return new CrewMeetingFeedCache(
			normalize(cachedPosts),
			hasMorePage
		);
	}

	private List<CachedCrewPost> normalize(List<CachedCrewPost> cachedPosts) {
		Map<Long, CachedCrewPost> deduplicatedPosts = new LinkedHashMap<>();

		for (CachedCrewPost cachedPost : cachedPosts) {
			deduplicatedPosts.put(cachedPost.id(), cachedPost);
		}

		return deduplicatedPosts.values().stream()
			.sorted(
				Comparator.comparing(CachedCrewPost::createdDate)
					.reversed()
					.thenComparing(CachedCrewPost::id, Comparator.reverseOrder())
			)
			.toList();
	}

	private CrewMeetingFeedCache readCache(Long userId, LocalDateTime snapshotTime) {
		String cacheKey = generateCacheKey(userId, snapshotTime);

		Object cachedValue = redisTemplate.opsForValue().get(cacheKey);

		if (!(cachedValue instanceof String cachedJson)) {
			return CrewMeetingFeedCache.empty();
		}

		try {
			return objectMapper.readValue(cachedJson, CrewMeetingFeedCache.class);
		} catch (Exception exception) {
			log.warn("모임 피드 캐시 역직렬화 실패. cacheKey: {}", cacheKey, exception);
			return CrewMeetingFeedCache.empty();
		}
	}

	private void writeCache(Long userId, LocalDateTime snapshotTime, CrewMeetingFeedCache cache) {
		String cacheKey = generateCacheKey(userId, snapshotTime);

		try {
			String cacheJson = objectMapper.writeValueAsString(cache);
			redisTemplate.opsForValue().set(cacheKey, cacheJson, CACHE_TTL);
		} catch (Exception exception) {
			log.warn("모임 피드 캐시 저장 실패. cacheKey: {}", cacheKey, exception);
		}
	}

	private String generateCacheKey(Long userId, LocalDateTime snapshotTime) {
		return CACHE_PREFIX
			+ userId
			+ ":"
			+ snapshotTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}
}