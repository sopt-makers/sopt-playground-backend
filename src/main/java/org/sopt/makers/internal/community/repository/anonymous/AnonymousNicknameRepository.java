package org.sopt.makers.internal.community.repository.anonymous;

import java.util.List;

import org.sopt.makers.internal.community.domain.anonymous.AnonymousNickname;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnonymousNicknameRepository extends JpaRepository<AnonymousNickname, Long> {

	// CREATE

	// READ
	@Query(value = "SELECT * FROM internal_dev.anonymous_nickname WHERE anonymous_nickname_id NOT IN (:excludes) ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
	AnonymousNickname findRandomOneByIdNotIn(@Param("excludes") List<Long> excludes);

	@Query(value = "SELECT * FROM internal_dev.anonymous_nickname ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
	AnonymousNickname findRandomOne();

	// UPDATE

	// DELETE


}
