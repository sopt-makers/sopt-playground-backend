package org.sopt.makers.internal.popup.repository;

import org.sopt.makers.internal.popup.domain.Popup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PopupRepository extends JpaRepository<Popup, Long> {

    @Query(value = "SELECT * FROM popup WHERE :currentDate BETWEEN start_date AND end_date ORDER BY start_date ASC LIMIT 1", nativeQuery = true)
    Optional<Popup> findFirstCurrentPopup(@Param("currentDate") LocalDate currentDate);
}