package org.sopt.makers.internal.resolution.domain;

import lombok.*;
import org.sopt.makers.internal.common.util.GenericEnumListConverter;
import org.sopt.makers.internal.member.domain.Member;
import org.sopt.makers.internal.common.AuditingTimeEntity;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class UserResolution extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer generation;

    @Convert(converter = ResolutionTagConverter.class)
    private List<ResolutionTag> resolutionTags;

    @Converter(autoApply = true)
    public static class ResolutionTagConverter extends GenericEnumListConverter<ResolutionTag> {
        public ResolutionTagConverter() {
            super(ResolutionTag.class);
        }
    }
}
