package org.sopt.makers.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.EmailSender;
import org.sopt.makers.internal.dto.member.CoffeeChatRequest;
import org.sopt.makers.internal.exception.BusinessLogicException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.repository.MemberRepository;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@Service
public class CoffeeChatService {
    private final EmailSender emailSender;
    private final MemberRepository memberRepository;

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
        } catch (MessagingException | UnsupportedEncodingException exception) {
            exception.printStackTrace();
            throw new BusinessLogicException("커피챗 이메일 전송에 실패했습니다.");
        }

    }
}
