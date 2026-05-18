package org.sopt.makers.internal.external.pushNotification.message.member;

public final class MemberPushConstants {

	private MemberPushConstants() {
		throw new UnsupportedOperationException("유틸 클래스는 인스턴스화 할 수 없습니다.");
	}

	// 에스크 질문 알림
	public static final String QUESTION_NOTIFICATION_TITLE = "💬나의 에스크에 질문이 달렸어요.";
	public static final String QUESTION_CONTENT_FORMAT = "[이런 내용이 궁금해요] : \"%s\"";

	// 에스크 답변 알림
	public static final String ANSWER_NOTIFICATION_TITLE = "💬나의 에스크에 답변이 달렸어요.";
	public static final String ANSWER_CONTENT_FORMAT = "[%s의 댓글] : \"%s\"";

	// 답변 내용 최대 길이
	public static final int CONTENT_MAX_LENGTH = 100;
}
