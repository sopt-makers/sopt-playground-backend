package org.sopt.makers.internal.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.member.domain.enums.ServiceType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "appjam_tl_members")
public class TlMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "tl_generation", nullable = false)
    private Integer tlGeneration;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Column(name = "self_introduction", nullable = false, length = 2048)
    private String selfIntroduction;

    @Column(name = "competition_data", nullable = false, length = 2048)
    private String competitionData;
}
