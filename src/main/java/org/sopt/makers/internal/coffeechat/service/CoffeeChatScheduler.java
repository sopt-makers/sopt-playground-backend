package org.sopt.makers.internal.coffeechat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.coffeechat.dto.response.RandomCoffeeChatResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CoffeeChatScheduler {

    private static final Duration RANDOM_COFFEE_CHAT_TTL = Duration.ofHours(25);

    private final CoffeeChatService coffeeChatService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        refreshRandomCoffeeChats();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void refreshRandomCoffeeChats() {
        try {
            List<RandomCoffeeChatResponse> result = coffeeChatService.buildRandomCoffeeChatList();
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(CoffeeChatService.RANDOM_COFFEE_CHAT_REDIS_KEY, json, RANDOM_COFFEE_CHAT_TTL);
            log.info("[CoffeeChatScheduler] 랜덤 커피챗 캐시 갱신 완료");
        } catch (Exception e) {
            log.error("[CoffeeChatScheduler] 랜덤 커피챗 캐시 갱신 실패", e);
        }
    }
}
