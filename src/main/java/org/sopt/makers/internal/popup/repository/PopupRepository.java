package org.sopt.makers.internal.popup.repository;

import org.sopt.makers.internal.popup.domain.Popup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface PopupRepository extends JpaRepository<Popup, Long> {

    @Query("SELECT p FROM Popup p WHERE :currentDate BETWEEN p.startDate AND p.endDate ORDER BY p.startDate ASC")
    Optional<Popup> findFirstCurrentPopup(@Param("currentDate") LocalDate currentDate, Pageable pageable);
}