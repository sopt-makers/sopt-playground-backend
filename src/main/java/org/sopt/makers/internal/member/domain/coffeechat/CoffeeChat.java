package org.sopt.makers.internal.member.domain.coffeechat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.hibernate.annotations.ColumnDefault;
import org.sopt.makers.internal.common.GenericEnumListConverter;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.common.AuditingTimeEntity;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CoffeeChat extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(nullable = false)
    @ColumnDefault("true")
    private Boolean isCoffeeChatActivate = true;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Career career;

    @Column(nullable = false, length = 200)
    private String introduction;

    @Convert(converter = CoffeeChatSectionConverter.class)
    @Column(nullable = false)
    private List<CoffeeChatSection> section;

    @Column(nullable = false, length = 40)
    private String coffeeChatBio;

    @Convert(converter = CoffeeChatTopicTypeConverter.class)
    @Column(nullable = false)
    private List<CoffeeChatTopicType> coffeeChatTopicType;

    @Column(nullable = false, length = 1000)
    private String topic;

    @Column
    @Enumerated(EnumType.STRING)
    private MeetingType meetingType;

    @Column(length = 1000)
    private String guideline;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void updateCoffeeChatInformation(Boolean isCoffeeChatActivate, String coffeeChatBio) {
        this.isCoffeeChatActivate = isCoffeeChatActivate;
        this.coffeeChatBio = coffeeChatBio;
    }

    public void updateCoffeeChatInfo(Career career, String introduction, List<CoffeeChatSection> sections, String coffeeChatBio, List<CoffeeChatTopicType> coffeeChatTopicType, String topic, MeetingType meetingType, String guideline) {
        this.career = career;
        this.introduction = introduction;
        this.section = sections;
        this.coffeeChatBio = coffeeChatBio;
        this.coffeeChatTopicType = coffeeChatTopicType;
        this.topic = topic;
        this.meetingType = meetingType;
        this.guideline = guideline;
    }

    @Converter(autoApply = true)
    public static class CoffeeChatSectionConverter extends GenericEnumListConverter<CoffeeChatSection> {
        public CoffeeChatSectionConverter() {
            super(CoffeeChatSection.class);
        }
    }

    @Converter(autoApply = true)
    public static class CoffeeChatTopicTypeConverter extends GenericEnumListConverter<CoffeeChatTopicType> {
        public CoffeeChatTopicTypeConverter() {
            super(CoffeeChatTopicType.class);
        }
    }
}
