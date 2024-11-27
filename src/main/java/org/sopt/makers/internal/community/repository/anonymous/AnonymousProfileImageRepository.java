package org.sopt.makers.internal.community.repository.anonymous;

import org.sopt.makers.internal.community.domain.AnonymousProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnonymousProfileImageRepository extends JpaRepository<AnonymousProfileImage, Long> {

    List<AnonymousProfileImage> findAllByIdNot(Long id);
}
