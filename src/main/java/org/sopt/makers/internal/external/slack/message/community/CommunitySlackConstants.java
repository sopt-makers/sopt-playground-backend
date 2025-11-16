package org.sopt.makers.internal.external.slack.message.community;

public final class CommunitySlackConstants {

    private CommunitySlackConstants() {
        throw new UnsupportedOperationException("유틸 클래스는 인스턴스화 할 수 없습니다.");
    }

    // 댓글 신고 관련 메시지
    public static final String COMMENT_REPORT_TITLE = "🚨댓글 신고 발생!🚨";
    public static final String COMMENT_REPORT_HEADER = "댓글 신고가 들어왔어요!";
    public static final String COMMENT_REPORT_REPORTER_LABEL = "*신고자:*\n";
    public static final String COMMENT_REPORT_CONTENT_LABEL = "*댓글 내용:*\n";
    public static final String COMMENT_REPORT_LINK_LABEL = "*링크:*\n";
    public static final String COMMENT_REPORT_LINK_FORMAT = "<https://playground.sopt.org/feed/%d|글>";
}
