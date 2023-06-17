package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.WordChainGameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WordChainGameRepository extends JpaRepository<WordChainGameRoom, Long> {

    Optional<WordChainGameRoom> findByIdOrderByCreatedAtDesc(Long roomId);
}