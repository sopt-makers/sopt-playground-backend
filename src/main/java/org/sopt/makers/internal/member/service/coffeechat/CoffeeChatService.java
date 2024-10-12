package org.sopt.makers.internal.member.service.coffeechat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sopt.makers.internal.domain.Member;
import org.sopt.makers.internal.domain.MemberCareer;
import org.sopt.makers.internal.dto.member.CoffeeChatRequest;
import org.sopt.makers.internal.dto.member.CoffeeChatResponse.CoffeeChatVo;
import org.sopt.makers.internal.external.MessageSender;
import org.sopt.makers.internal.external.MessageSenderFactory;
import org.sopt.makers.internal.member.domain.coffeechat.ChatCategory;
import org.sopt.makers.internal.member.domain.coffeechat.CoffeeChat;
import org.sopt.makers.internal.member.mapper.coffeechat.CoffeeChatResponseMapper;
import org.sopt.makers.internal.member.service.MemberRetriever;
import org.sopt.makers.internal.member.service.career.MemberCareerRetriever;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CoffeeChatService {
    private final MessageSenderFactory messageSenderFactory;

    private final MemberRetriever memberRetriever;
    private final MemberCareerRetriever memberCareerRetriever;

    private final EmailHistoryService emailHistoryService;

    private final CoffeeChatCreator coffeeChatCreator;
    private final CoffeeChatRetriever coffeeChatRetriever;

    private final CoffeeChatResponseMapper coffeeChatResponseMapper;

    @Transactional
    public void sendCoffeeChatRequest (CoffeeChatRequest request, Long senderId) {
        Member receiver  = memberRetriever.findMemberById(request.receiverId());
        Member sender = memberRetriever.findMemberById(senderId);

        String replyInfo;
        if (!request.category().equals(ChatCategory.COFFEE_CHAT)) {
            replyInfo = applyDefaultPhone(request.senderPhone(), sender.getPhone());
        } else {
            replyInfo = applyDefaultEmail(request.senderEmail(), sender.getEmail());
        }

        MessageSender senderStrategy = messageSenderFactory.getSender(request.category());
        senderStrategy.sendMessage(sender, receiver, request.content(), replyInfo, request.category());

        if (!request.category().equals(ChatCategory.COFFEE_CHAT)) {
            emailHistoryService.createEmailHistory(request, sender, sender.getEmail());
        } else {
            coffeeChatCreator.createCoffeeChatHistory(sender, receiver, request.content());
        }
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
