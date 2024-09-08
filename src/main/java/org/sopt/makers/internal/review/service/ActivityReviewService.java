package org.sopt.makers.internal.review.service;

import static org.sopt.makers.internal.common.Constant.*;
import static org.sopt.makers.internal.service.MemberServiceUtil.*;

import org.sopt.makers.internal.exception.ClientBadRequestException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.review.domain.ActivityReview;
import org.sopt.makers.internal.review.dto.request.CreateActivityReviewRequest;
import org.sopt.makers.internal.review.dto.response.ActivityReviewResponse;
import org.sopt.makers.internal.review.dto.response.PagedActivityReviewResponse;
import org.sopt.makers.internal.review.repository.ActivityReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityReviewService {

    private final ActivityReviewRepository activityReviewRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createActivityReview(CreateActivityReviewRequest request, Long memberId) {
        val member = findMemberById(memberRepository, memberId);
        if (!member.getGeneration().equals(CURRENT_GENERATION)) {
            throw new ClientBadRequestException("Only current generation can write activity reviews");
        }
        val activityReview = ActivityReview.builder()
                .member(member)
                .content(request.content())
                .generation(CURRENT_GENERATION).build();
        activityReviewRepository.save(activityReview);
    }

    @Transactional(readOnly = true)
    public PagedActivityReviewResponse getActivityReviews(Pageable pageable) {
        Page<ActivityReview> reviewPage = activityReviewRepository.findAllByGeneration(CURRENT_GENERATION, pageable);
        List<ActivityReviewResponse> reviews = reviewPage.getContent().stream()
                .sorted((review1, review2) -> review2.getCreatedAt().compareTo(review1.getCreatedAt()))
                .map(review -> new ActivityReviewResponse(review.getId(), review.getContent()))
                .toList();
        return new PagedActivityReviewResponse(reviews, reviewPage.hasNext());
    }
}
