package org.sopt.makers.internal.review.service;

import static org.sopt.makers.internal.service.MemberServiceUtil.*;

import org.sopt.makers.internal.repository.MemberRepository;
import org.sopt.makers.internal.review.domain.ActivityReview;
import org.sopt.makers.internal.review.dto.request.CreateActivityReviewRequest;
import org.sopt.makers.internal.review.repository.ActivityReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@RequiredArgsConstructor
public class ActivityReviewService {

	private final ActivityReviewRepository activityReviewRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public void createActivityReview(CreateActivityReviewRequest request, Long memberId) {
		val member = findMemberById(memberRepository, memberId);
		val activityReview = ActivityReview.builder()
				.member(member)
				.content(request.content()).build();
		activityReviewRepository.save(activityReview);
	}
}
