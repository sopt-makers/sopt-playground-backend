package org.sopt.makers.internal.resolution.domain;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.common.AuditingTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_resolution_lucky_pick")
public class UserResolutionLuckyPick extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long memberId;

    @Column(nullable = false)
    private boolean result = false;

    @Column(nullable = false)
    private boolean hasDrawn = false;

    @Builder
    public UserResolutionLuckyPick(Long memberId) {
        this.memberId = memberId;
    }

    public void win() {
        this.result = true;
    }

    public void draw() {
        this.hasDrawn = true;
    }
}
