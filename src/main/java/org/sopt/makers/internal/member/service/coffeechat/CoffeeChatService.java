package org.sopt.makers.internal.member.service.coffeechat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.domain.EmailHistory;
import org.sopt.makers.internal.domain.EmailSender;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.dto.member.CoffeeChatRequest;
import org.sopt.makers.internal.dto.member.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.exception.BusinessLogicException;
import org.sopt.makers.internal.external.gabia.GabiaService;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.sopt.makers.internal.member.domain.coffeechat.ChatCategory;
import org.sopt.makers.internal.member.mapper.coffeechat.CoffeeChatResponseMapper;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.sopt.makers.internal.repository.EmailHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CoffeeChatService {
    private final EmailSender emailSender;
    private final EmailHistoryRepository emailHistoryRepository;

    private final GabiaService gabiaService;

    private final MemberRetriever memberRetriever;

    private final CoffeeChatCreator coffeeChatCreator;
    private final CoffeeChatRetriever coffeeChatRetriever;

    private final CoffeeChatResponseMapper coffeeChatResponseMapper;

    private final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final MemberCareerRetriever memberCareerRetriever;

    @Transactional
    public void sendCoffeeChatRequest (CoffeeChatRequest request, Long senderId) {
        Member receiver  = memberRetriever.findMemberById(request.receiverId());
        Member sender = memberRetriever.findMemberById(senderId);
        if (request.category().equals(ChatCategory.COFFEE_CHAT.getValue())) {
            String phone = applyDefaultPhone(request.senderPhone(), sender.getPhone());

            sendCoffeeChatSMS(request, sender, receiver, phone);
            coffeeChatCreator.createCoffeeChatHistory(sender, receiver, request.content());
        } else {
            String email = applyDefaultEmail(request.senderEmail(), sender.getEmail());

            sendChatEmail(request, sender, receiver, email);
            createEmailHistory(request, sender, email);
        }
    }

    private void sendChatEmail(CoffeeChatRequest request, Member sender, Member receiver, String senderEmail) {
        String html = emailSender.createCoffeeChatEmailHtml(
                sender.getName(),
                senderEmail,
                sender.getId(),
                request.category(),
                sender.getProfileImage(),
                request.content()
        );
        String subject = """
            띠링, %s님이 보낸 쪽지가 도착했어요!""".formatted(sender.getName());
        try {
            emailSender.sendEmail(
                    receiver.getEmail(),
                    subject,
                    html
            );
        } catch (MessagingException | UnsupportedEncodingException exception) {
            log.error(exception.getMessage());
            throw new BusinessLogicException("커피챗 이메일 전송에 실패했습니다.");
        }
    }

    private void sendCoffeeChatSMS(CoffeeChatRequest request, Member sender, Member receiver, String senderPhone) {
        String message = "[Web발신][SOPT makers] 커피챗 신청이 도착했어요!\n" +
                "전달드린 전화번호로 직접 연결어쩌고해저쩌고주세요.\n\n" +
                "- 이름 : " + sender.getName() + "\n" +
                "- 연락처 : " + senderPhone + "\n" +
                "- 파트 : " + memberRetriever.concatPartAndGeneration(sender.getId()) + "\n" +
                "- 멤버 프로필 링크 : https://playground.sopt.org/members/" + sender.getId() + "\n\n" +
                "- 이런 내용이 궁금해요\n" +
                request.content();

        gabiaService.sendSMS(receiver.getPhone(), message);
    }

    @Transactional(readOnly = true)
    public List<CoffeeChatVo> getCoffeeChatActivateMemberList () {

        List<CoffeeChat> coffeeChatActivateList = coffeeChatRetriever.findCoffeeChatActivate(true);
        List<Member> memberList = coffeeChatActivateList.stream().map(CoffeeChat::getMember).toList();
        List<MemberCareer> careerList = memberList.stream().map(member -> memberCareerRetriever.findMemberLastCareerByMemberId(member.getId())).toList();

        List<CoffeeChatVo> coffeeChatVoList = coffeeChatResponseMapper.toCoffeeChatResponse(coffeeChatActivateList, memberList, careerList);
        Collections.shuffle(coffeeChatVoList);
        return coffeeChatVoList;
    }

    @Transactional
    public void createCoffeeChat (Long memberId, String coffeeChatBio) {
        Member member = memberRetriever.findMemberById(memberId);

        coffeeChatRetriever.checkAlreadyExistCoffeeChat(member);
        coffeeChatCreator.createCoffeeChat(member, coffeeChatBio);
    }

    private void createEmailHistory(CoffeeChatRequest request, Member sender, String email) {
        emailHistoryRepository.save(EmailHistory.builder()
                .senderId(sender.getId())
                .receiverId(request.receiverId())
                .senderEmail(email)
                .category(request.category())
                .content(request.content())
                .createdAt(LocalDateTime.now(KST)).build());
    }

    private String applyDefaultEmail(String requestEmail, String senderEmail) {
        if (requestEmail == null) {
            return senderEmail;
        }
        return requestEmail;
    }

    private String applyDefaultPhone(String requestPhone, String senderPhone) {
        if (requestPhone == null) {
            return senderPhone;
        }
        return requestPhone;
    }
}
