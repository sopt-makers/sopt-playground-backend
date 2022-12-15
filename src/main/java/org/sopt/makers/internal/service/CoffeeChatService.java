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

    public void sendCoffeeChatRequest (CoffeeChatRequest request) {
        val receiver  = memberRepository.findById(request.receiverId())
                .orElseThrow(() -> new NotFoundDBEntityException("Member"));

        try {
            emailSender.sendEmail(
                    receiver.getEmail(),
                    request.senderEmail(),
                    "SUBJECT",
                    "HTML"
            );
        } catch (MessagingException | UnsupportedEncodingException exception) {
            exception.printStackTrace();
            throw new BusinessLogicException("커피챗 이메일 전송에 실패했습니다.");
        }

    }
}
