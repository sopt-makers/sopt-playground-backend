package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.EmailHistory;
import org.sopt.makers.internal.domain.EmailSender;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.dto.member.CoffeeChatRequest;
import org.sopt.makers.internal.dto.member.CoffeeChatResponse;
import org.sopt.makers.internal.dto.member.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.exception.BusinessLogicException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.EmailHistoryRepository;
import org.sopt.makers.internal.repository.MemberProfileQueryRepository;
import org.sopt.makers.internal.repository.MemberRepository;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CoffeeChatService {
    private final EmailSender emailSender;
    private final MemberRepository memberRepository;
    private final EmailHistoryRepository emailHistoryRepository;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");

    public void sendCoffeeChatRequest (CoffeeChatRequest request, Long senderId) {
        val receiver  = memberRepository.findById(request.receiverId())
                .orElseThrow(() -> new NotFoundDBEntityException("Member"));
        val sender = memberRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundDBEntityException("Member"));
        val html = emailSender.createCoffeeChatEmailHtml(
                sender.getName(),
                request.senderEmail(),
                senderId,
                request.category(),
                sender.getProfileImage(),
                request.content()
        );
        val subject = """
                띠링, %s님이 보낸 쪽지가 도착했어요!""".formatted(sender.getName());
        try {
            emailSender.sendEmail(
                    receiver.getEmail(),
                    subject,
                    html
            );

            emailHistoryRepository.save(EmailHistory.builder()
                            .senderId(senderId)
                            .receiverId(request.receiverId())
                            .senderEmail(request.senderEmail())
                            .category(request.category())
                            .content(request.content())
                            .createdAt(LocalDateTime.now(KST)).build());
        } catch (MessagingException | UnsupportedEncodingException exception) {
            exception.printStackTrace();
            throw new BusinessLogicException("커피챗 이메일 전송에 실패했습니다.");
        }

    }

    public List<CoffeeChatVo> getCoffeeChatList () {
        val members = memberRepository.findAllByIsCoffeeChatActivateTrue();
        return members.stream().map(
            m -> {
                val career = getCurrentMemberCareer(m);
                return new CoffeeChatVo(
                    m.getId(), m.getName(), m.getProfileImage(),
                    career == null ? m.getUniversity() : career.getCompanyName(),
                    career == null ? m.getSkill() : career.getTitle(),
                    m.getCoffeeChatBio()
                );
            }
        ).toList();
    }

    private MemberCareer getCurrentMemberCareer(Member member) {
        List<MemberCareer> careers = member.getCareers();
        if (!careers.isEmpty()) {
            return careers.get(careers.size() - 1);
        }
        return null;
    }
}
