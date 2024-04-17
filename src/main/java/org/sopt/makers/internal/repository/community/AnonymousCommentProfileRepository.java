package org.sopt.makers.internal.repository.community;

import org.sopt.makers.internal.domain.community.AnonymousPostCommentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnonymousPostCommentProfileRepository extends JpaRepository<AnonymousPostCommentProfile, Long> {
}
