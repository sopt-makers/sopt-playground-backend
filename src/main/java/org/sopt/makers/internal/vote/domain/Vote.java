package org.sopt.makers.internal.vote.domain;

import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.sopt.makers.internal.community.domain.CommunityPost;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private CommunityPost post;

    @Column(nullable = false)
    private boolean isMultipleOptions;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<VoteOption> voteOptions = new ArrayList<>();

    public static Vote of(CommunityPost post, boolean isMultiple, List<String> optionContents) {
        Vote vote = new Vote();
        vote.post = post;
        vote.isMultipleOptions = isMultiple;
        vote.voteOptions = optionContents.stream()
                .map(content -> VoteOption.of(vote, content))
                .toList();
        return vote;
    }
}
