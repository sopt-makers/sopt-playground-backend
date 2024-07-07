package org.sopt.makers.internal.scheduler;

import java.util.List;

import org.sopt.makers.internal.community.service.CommunityPostService;
import org.sopt.makers.internal.domain.community.CommunityPost;
import org.sopt.makers.internal.dto.pushNotification.PushNotificationRequest;
import org.sopt.makers.internal.service.PushNotificationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PushNotificationScheduler {

    private static final String NOTIFICATION_TITLE = "🔥 어제 가장 인기있는 게시글이에요.";
    private static final String NOTIFICATION_CATEGORY = "NEWS";

    private final PushNotificationService pushNotificationService;
    private final CommunityPostService communityPostService;

    @Scheduled(cron = "0 40 11 * * ?")
    public void sendHotPostPushNotification() {
        List<CommunityPost> todayCommunityPosts = communityPostService.getTodayPosts();
        CommunityPost hotPost = communityPostService.findHotPost(todayCommunityPosts);

        if (hotPost != null) {
            sendNotificationForHotPost(hotPost);
        }
    }

    private void sendNotificationForHotPost(CommunityPost hotPost) {
        String webLink = "https://playground.sopt.org/?feed=" + hotPost.getId();
        PushNotificationRequest pushNotificationRequest = PushNotificationRequest.builder()
                .title(NOTIFICATION_TITLE)
                .content(hotPost.getTitle())
                .category(NOTIFICATION_CATEGORY)
                .webLink(webLink)
                .build();

        pushNotificationService.sendAllPushNotification(pushNotificationRequest);
    }
}