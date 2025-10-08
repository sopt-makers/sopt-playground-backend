package org.sopt.makers.internal.wordchaingame.domain;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "word_chain_gameroom")
public class WordChainGameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_word")
    String startWord;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_user_id")
    private Long createdUserId;

    @Builder.Default
    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @JoinColumn(name = "room_id")
    private List<Word> wordList = new ArrayList<>();
}
