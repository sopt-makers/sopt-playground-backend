package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.WordChainGameWinner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordChainGameWinnerRepository extends JpaRepository<WordChainGameWinner, Long> {
    WordChainGameWinner findFirstByUserIdOrderByIdDesc(Long userId);
}
