package org.sopt.makers.internal.repository;

import org.sopt.makers.internal.domain.EmailHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailHistoryRepository extends JpaRepository<EmailHistory, Long> {
}
