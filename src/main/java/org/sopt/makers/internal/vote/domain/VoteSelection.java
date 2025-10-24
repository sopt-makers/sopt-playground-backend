package org.sopt.makers.internal.vote.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.member.domain.Member;

import jakarta.persistence.*;

@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "vote_option_id"}))
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoteSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vote_option_id", nullable = false)
    private VoteOption voteOption;

    public static VoteSelection of(Member member, VoteOption voteOption) {
        VoteSelection selection = new VoteSelection();
        selection.member = member;
        selection.voteOption = voteOption;
        return selection;
    }
}