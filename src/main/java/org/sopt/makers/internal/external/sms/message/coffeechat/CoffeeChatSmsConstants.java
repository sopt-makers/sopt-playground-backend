package org.sopt.makers.internal.external.sms.message.coffeechat;

public class CoffeeChatSmsConstants {

    private CoffeeChatSmsConstants() {
        throw new IllegalStateException("Utility class");
    }

    // 메시지 헤더
    public static final String MESSAGE_TITLE = "[SOPT makers] 쪽지가 도착했어요!";

    // 필드 레이블
    public static final String NAME_LABEL = "[이름] ";
    public static final String PART_LABEL = "[파트] ";
    public static final String TOPIC_LABEL = "[주제] ";
    public static final String CONTENT_LABEL = "[이런 내용이 궁금해요]\n";
    public static final String PROFILE_LINK_LABEL = "[멤버 프로필 링크]\n";
    public static final String PHONE_LABEL = "[연락처] ";

    // 안내 메시지
    public static final String GUIDE_MESSAGE = "나의 조언이 용기 낸 SOPT 동료에게 큰 도움이 될 수 있어요. \n" +
            "쪽지에 응하신다면 아래 동료의 전화번호로 직접 연락해 주세요.";

    // URL 포맷
    public static final String PROFILE_URL_FORMAT = "https://playground.sopt.org/members/%d";

    // 주제 매핑
    public static final String TOPIC_COFFEE_CHAT = "커피챗";
    public static final String TOPIC_NOTE = "친목";
}
