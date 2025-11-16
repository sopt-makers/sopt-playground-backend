package org.sopt.makers.internal.external.pushNotification.message.community;

public final class CommunityPushConstants {

    private CommunityPushConstants() {
        throw new UnsupportedOperationException("유틸 클래스는 인스턴스화 할 수 없습니다.");
    }

    // 댓글 알림
    public static final String COMMENT_NOTIFICATION_TITLE = "💬나의 게시글에 새로운 댓글이 달렸어요.";
    public static final String COMMENT_WRITER_ANONYMOUS = "익명";
    public static final String COMMENT_CONTENT_FORMAT = "[%s의 댓글] : \"%s\"";

    // 답글 알림
    public static final String REPLY_NOTIFICATION_TITLE = "💬나의 댓글에 새로운 답글이 달렸어요.";
    public static final String REPLY_CONTENT_FORMAT = "[%s의 답글] : \"%s\"";

    // 멘션 알림
    public static final String MENTION_NOTIFICATION_TITLE_FORMAT = "💬%s님이 회원님을 언급했어요.";
    public static final String MENTION_CONTENT_FORMAT = "\"%s\"";

    // 댓글 내용 최대 길이
    public static final int CONTENT_MAX_LENGTH = 100;
}
