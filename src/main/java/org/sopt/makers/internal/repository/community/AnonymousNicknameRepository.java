package org.sopt.makers.internal.repository.community;

import java.util.List;

import org.sopt.makers.internal.domain.community.AnonymousNickname;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AnonymousNicknameRepository extends JpaRepository<AnonymousNickname, Long> {

	@Query(value = "SELECT * FROM internal_dev.anonymous_nickname WHERE anonymous_nickname_id NOT IN (:excludes) ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
	AnonymousNickname findRandomOneByIdNotIn(List<Long> excludes);

	@Query(value = "SELECT * FROM internal_dev.anonymous_nickname ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
	AnonymousNickname findRandomOne();

}
