package org.sopt.makers.internal.vote.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int voteCount;

    @OneToMany(mappedBy = "voteOption", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VoteSelection> voteSelections = new ArrayList<>();

    public static VoteOption of(Vote vote, String content) {
        VoteOption option = new VoteOption();
        option.vote = vote;
        option.content = content;
        option.voteCount = 0;
        return option;
    }

    public void increaseCount() {
        this.voteCount++;
    }
}
