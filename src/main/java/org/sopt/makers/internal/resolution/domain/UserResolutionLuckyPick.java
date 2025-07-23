package org.sopt.makers.internal.resolution.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.common.AuditingTimeEntity;

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
