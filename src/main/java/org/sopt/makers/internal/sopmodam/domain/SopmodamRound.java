package org.sopt.makers.internal.sopmodam.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.common.AuditingTimeEntity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "sopmodam_round")
public class SopmodamRound extends AuditingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer generation;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private LocalDateTime voteStartAt;

    @Column(nullable = false)
    private LocalDateTime voteEndAt;

    private String selectedQuestion;

    @Column(nullable = false)
    private LocalDateTime answerEndAt;

    public void updateRound(
        Integer generation,
        String eventName,
        LocalDateTime voteStartAt,
        LocalDateTime voteEndAt,
        LocalDateTime answerEndAt
    ) {
        this.generation = generation;
        this.eventName = eventName;
        this.voteStartAt = voteStartAt;
        this.voteEndAt = voteEndAt;
        this.answerEndAt = answerEndAt;
    }
}
