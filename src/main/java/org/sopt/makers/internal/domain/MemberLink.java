package org.sopt.makers.internal.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "member_links")
public class MemberLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long memberId;

    @Column(name = "title")
    private String title;

    @Column(name = "url")
    private String url;

    public void setMemberId (Long memberId) {
        this.memberId = memberId;
    }

}
