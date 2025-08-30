package org.sopt.makers.internal.wordchaingame.repository;

import org.sopt.makers.internal.wordchaingame.domain.WordChainGameWinner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordChainGameWinnerRepository extends JpaRepository<WordChainGameWinner, Long> {
    WordChainGameWinner findFirstByUserIdOrderByIdDesc(Long userId);
    List<WordChainGameWinner> findAllByOrderByIdDesc();
}
