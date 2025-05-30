package org.sopt.makers.internal.wordchaingame.repository;

import org.sopt.makers.internal.wordchaingame.domain.WordChainGameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordChainGameRepository extends JpaRepository<WordChainGameRoom, Long> {
    boolean existsByIdIsNotNull();
}