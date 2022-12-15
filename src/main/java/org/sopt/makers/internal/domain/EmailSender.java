package org.sopt.makers.internal.domain;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.config.AuthConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
public class EmailSender {

    private final AuthConfig authConfig;
    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String html) throws MessagingException, UnsupportedEncodingException {
        val message = mailSender.createMimeMessage();
        val helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom(authConfig.getFromEmail());
        helper.setTo(to);
        helper.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));
        helper.setText(html, true);
        mailSender.send(message);
    }

    public void sendEmail(String to, String from, String subject, String html) throws MessagingException, UnsupportedEncodingException {
        val message = mailSender.createMimeMessage();
        val helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));
        helper.setText(html, true);
        mailSender.send(message);
    }

    public String createRegisterEmailHtml (String token) {
        val registerPageUriTemplate = authConfig.getRegisterPage();
        val registerPageUri = registerPageUriTemplate.replace("{{token}}", token);

        return """
                        <div style="text-align:center;font-weight:400;">
                            <br><br><br>
                            <h1 style="font-size: 32px;">SOPT 회원인증 완료</h1>
                            <p style="font-size: 16px;">SOPT 회원인증을 위한 메일입니다.<br>아래의 버튼을 눌러 회원가입 절차를 계속 진행해주세요.</p>
                            <br>
                            <a style="color: blue;" href="%s" target="_blank">회원가입 계속하기</a>
                            <br><br>
                        </div>
                """.formatted(registerPageUri);
    }
}
