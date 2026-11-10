package org.sopt.makers.internal.wordchaingame.domain;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(
    name = "word",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_word_room_id_word",
        columnNames = {"room_id", "word"}
    )
)
public class Word{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long memberId;

    @Column
    private String word;

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}