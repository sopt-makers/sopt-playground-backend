package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.Word;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {
}
