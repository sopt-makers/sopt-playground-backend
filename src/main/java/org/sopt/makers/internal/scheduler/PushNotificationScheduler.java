package org.sopt.makers.internal.scheduler;

import lombok.RequiredArgsConstructor;
import org.sopt.makers.internal.community.repository.CommunityPostLikeRepository;
import org.sopt.makers.internal.domain.community.CommunityPost;
import org.sopt.makers.internal.dto.pushNotification.PushNotificationRequest;
import org.sopt.makers.internal.repository.community.CommunityCommentRepository;
import org.sopt.makers.internal.repository.community.CommunityPostRepository;
import org.sopt.makers.internal.service.PushNotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PushNotificationScheduler {

    private static final int MIN_POINTS_FOR_HOT_POST = 10;
    private static final String NOTIFICATION_TITLE = "";
    private static final String NOTIFICATION_CONTENT = "🔥 어제 가장 인기있는 게시글이에요.";
    private static final String NOTIFICATION_CATEGORY = "NEWS";

    private final PushNotificationService pushNotificationService;
    private final CommunityPostRepository communityPostRepository;
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityPostLikeRepository communityPostLikeRepository;

    @Scheduled(cron = "0 40 11 * * ?")
    public void sendHotPostPushNotification() {
        LocalDate yesterday = LocalDate.now();
        LocalDateTime startOfDay = yesterday.atStartOfDay().minusHours(9);
        LocalDateTime endOfDay = yesterday.atTime(LocalTime.MAX).minusHours(9);
        List<CommunityPost> todayCommunityPosts = communityPostRepository.findAllByCreatedAtBetween(startOfDay, endOfDay);

        CommunityPost hotPost = findHotPost(todayCommunityPosts);

        if (hotPost != null) {
            sendNotificationForHotPost(hotPost);
        }
    }

    private CommunityPost findHotPost(List<CommunityPost> posts) {
        return posts.stream()
                .map(this::createPostWithPoints)
                .filter(post -> post.points() >= MIN_POINTS_FOR_HOT_POST)
                .max(Comparator.comparingInt(PostWithPoints::points)
                        .thenComparingInt(PostWithPoints::hits))
                .map(PostWithPoints::post)
                .orElse(null);
    }

    private PostWithPoints createPostWithPoints(CommunityPost post) {
        int commentCount = communityCommentRepository.countAllByPostId(post.getId());
        int likeCount = communityPostLikeRepository.countAllByPostId(post.getId());
        int points = calculatePoints(commentCount, likeCount);
        return new PostWithPoints(post, points, post.getHits());
    }

    private int calculatePoints(int commentCount, int likeCount) {
        return commentCount * 2 + likeCount;
    }

    private void sendNotificationForHotPost(CommunityPost hotPost) {
        String webLink = "https://playground.sopt.org/?feed=" + hotPost.getId();
        PushNotificationRequest pushNotificationRequest = PushNotificationRequest.builder()
                .title(NOTIFICATION_TITLE)
                .content(NOTIFICATION_CONTENT)
                .category(NOTIFICATION_CATEGORY)
                .webLink(webLink)
                .build();

        pushNotificationService.sendAllPushNotification(pushNotificationRequest);
    }

    private record PostWithPoints(CommunityPost post, int points, int hits) {
    }
}