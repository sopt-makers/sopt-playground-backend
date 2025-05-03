package org.sopt.makers.internal.external.message.email;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailHistoryRepository extends JpaRepository<EmailHistory, Long> {
}
