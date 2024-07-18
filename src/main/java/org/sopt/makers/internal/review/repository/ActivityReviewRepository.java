package org.sopt.makers.internal.review.repository;

import org.sopt.makers.internal.review.domain.ActivityReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityReviewRepository extends JpaRepository<ActivityReview, Long> {
}
