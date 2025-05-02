package org.sopt.makers.internal.wordchaingame.repository;

import org.sopt.makers.internal.wordchaingame.domain.WordChainGameWinner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordChainGameWinnerRepository extends JpaRepository<WordChainGameWinner, Long> {
    WordChainGameWinner findFirstByUserIdOrderByIdDesc(Long userId);
}
