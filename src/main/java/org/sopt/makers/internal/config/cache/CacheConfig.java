package org.sopt.makers.internal.config.cache;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@EnableCaching
@Configuration
public class CacheConfig {

	@Bean
	public List<CaffeineCache> caffeineCaches() {
		return Arrays.stream(CacheType.values()).map( cache ->
			new CaffeineCache(
				cache.getCacheName(),
				Caffeine.newBuilder().recordStats() // 통계 처리
					.expireAfterWrite(cache.getExpireAfterWriteOfSeconds(), TimeUnit.SECONDS) // TTL
					.maximumSize(cache.getMaximumSize())
					.build()
				)).collect(Collectors.toList());
		}

	@Bean
	public CacheManager cacheManager(List<CaffeineCache> caffeineCaches) {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(caffeineCaches);
		return cacheManager;
	}
}
