package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.WordChainGameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordChainGameRepository extends JpaRepository<WordChainGameRoom, Long> {
    boolean existsByIdIsNotNull();
}