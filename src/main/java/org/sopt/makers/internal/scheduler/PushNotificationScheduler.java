package org.sopt.makers.internal.scheduler;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PessimisticLockException;

import org.sopt.makers.internal.community.service.post.CommunityPostService;
import org.sopt.makers.internal.community.domain.CommunityPost;
import org.sopt.makers.internal.external.pushNotification.dto.PushNotificationRequest;
import org.sopt.makers.internal.external.pushNotification.PushNotificationService;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PushNotificationScheduler {

    private static final String NOTIFICATION_TITLE = "🔥 어제 가장 인기있는 게시글이에요.";
    private static final String NOTIFICATION_CATEGORY = "NEWS";

    private final PushNotificationService pushNotificationService;
    private final CommunityPostService communityPostService;
    private final PlatformTransactionManager transactionManager;

    @PersistenceContext
    private EntityManager em;

//    @Scheduled(cron = "0 40 11 * * ?")
    public void sendHotPostPushNotification() {
        List<CommunityPost> todayCommunityPosts = communityPostService.getTodayPosts();
        CommunityPost hotPost = communityPostService.findTodayHotPost(todayCommunityPosts);

        if (hotPost != null) {
            sendNotificationForHotPost(hotPost);
            updatePostIsHot(hotPost);
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

    private void updatePostIsHot(CommunityPost hotPost) {
        TransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);

        try {
            communityPostService.saveHotPost(hotPost);
            transactionManager.commit(transactionStatus);
        } catch (PessimisticLockingFailureException | PessimisticLockException e) {
            transactionManager.rollback(transactionStatus);
        } finally {
            em.close();
        }
    }
}