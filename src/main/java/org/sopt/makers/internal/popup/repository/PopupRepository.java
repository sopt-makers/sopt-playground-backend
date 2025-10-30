package org.sopt.makers.internal.popup.repository;

import org.sopt.makers.internal.popup.domain.Popup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopupRepository extends JpaRepository<Popup, Long> {
}