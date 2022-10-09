package org.sopt.makers.internal.domain;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String html) throws MessagingException {
        val message = mailSender.createMimeMessage();
        val helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom("2kwon2lee@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        message.setText(html, StandardCharsets.UTF_8.name());
        mailSender.send(message);
    }

    public String createRegisterEmailHtml (String token, String registerPageUriTemplate) {
        val registerPageUri = registerPageUriTemplate.replace("{{token}}", token);
        val buttonStyle = """
        background-color: ${backgroundColor};
        color: white;
        width: 420px;
        height: 48px;
        display: inline-block;
        line-height: 48px;
        text-decoration: none;
        font-size: 16px;
        border-radius: 6px;
        """;

        return """
                        <div style="text-align:center;font-weight:400;">
                            <br><br><br>
                            <h1 style="font-size: 32px;">SOPT 회원인증 완료</h1>
                            <p style="font-size: 16px;">SOPT 회원인증을 위한 메일입니다.<br>아래의 버튼을 눌러 회원가입 절차를 계속 진행해주세요.</p>
                            <br>
                            <a href="%s" target="_blank" style="%s">회원가입 계속하기</a>
                            <br><br>
                        </div>
                """.formatted(registerPageUri, buttonStyle);
    }
}
