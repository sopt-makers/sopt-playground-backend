package org.sopt.makers.internal.member.service.coffeechat;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.domain.EmailHistory;
import org.sopt.makers.internal.domain.EmailSender;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.dto.member.CoffeeChatRequest;
import org.sopt.makers.internal.dto.member.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.exception.BusinessLogicException;
import org.sopt.makers.internal.exception.NotFoundDBEntityException;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.sopt.makers.internal.member.mapper.coffeechat.CoffeeChatResponseMapper;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.sopt.makers.internal.repository.EmailHistoryRepository;
import org.sopt.makers.internal.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CoffeeChatService {
    private final EmailSender emailSender;
    private final MemberRepository memberRepository;
    private final EmailHistoryRepository emailHistoryRepository;

    private final MemberRetriever memberRetriever;

    private final CoffeeChatCreator coffeeChatCreator;
    private final CoffeeChatRetriever coffeeChatRetriever;

    private final CoffeeChatResponseMapper coffeeChatResponseMapper;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final MemberCareerRetriever memberCareerRetriever;

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

    @Transactional(readOnly = true)
    public List<CoffeeChatVo> getCoffeeChatActivateMemberList () {

        List<CoffeeChat> coffeeChatActivateList = coffeeChatRetriever.findCoffeeChatActivate(true);
        List<Member> memberList = coffeeChatActivateList.stream().map(CoffeeChat::getMember).toList();
        List<MemberCareer> careerList = memberList.stream().map(member -> memberCareerRetriever.findMemberLastCareerByMemberId(member.getId())).toList();
        Collections.shuffle(coffeeChatActivateList);

        return coffeeChatResponseMapper.toCoffeeChatResponse(coffeeChatActivateList, memberList, careerList);
    }

    @Transactional
    public void createCoffeeChat (Long memberId, String coffeeChatBio) {
        Member member = memberRetriever.findMemberById(memberId);

        coffeeChatRetriever.checkAlreadyExistCoffeeChat(member);
        coffeeChatCreator.createCoffeeChat(member, coffeeChatBio);
    }
}
