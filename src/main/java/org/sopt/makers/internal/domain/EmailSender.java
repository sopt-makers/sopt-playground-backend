package org.sopt.makers.internal.domain;

import lombok.AllArgsConstructor;
import lombok.val;
import org.sopt.makers.internal.config.AuthConfig;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;

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

    public String createCoffeeChatEmailHtml (
            String sender,
            String senderEmail,
            Long senderId,
            String category,
            String profilePicUrl,
            String content
    ) {
        val profileUrl = authConfig.getProfileUrl() + senderId;
        return """
                 <!DOCTYPE htmlPUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
                 <html lang="ko">
                 
                   <head>
                     <meta http-equiv="Content-Type" content="text/html charset=UTF-8" />
                   </head>
                   <div style="display:none;overflow:hidden;line-height:1px;opacity:0;max-height:0;max-width:0">%s님이 %s 주제로 쪽지를 보냈어요.<div>
                   </div>
                   <table style="width:100%%;background-color:#ffffff" align="center" border="0" cellPadding="0" cellSpacing="0" role="presentation">
                     <tbody>
                       <tr>
                         <td>
                           <div><!--[if mso | IE]>
                             <table role="presentation" width="100%%" align="center" style="max-width:37.5em;margin:0 auto;padding:20px 0 48px;width:580px;"><tr><td></td><td style="width:37.5em;background:#ffffff">
                           <![endif]--></div>
                           <div style="max-width:37.5em;margin:0 auto;padding:20px 0 48px;width:580px">
                             <p style="font-size:30px;line-height:1.3;margin:16px 0;font-family:-apple-system,BlinkMacSystemFont,&quot;Segoe UI&quot;,Roboto,Oxygen-Sans,Ubuntu,Cantarell,&quot;Helvetica Neue&quot;,sans-serif;font-weight:700;color:#484848">%s님이 %s 주제로 쪽지를 보냈어요.</p>
                             <table style="width:100%%;text-align:center;padding:30px 0" align="center" border="0" cellPadding="0" cellSpacing="0" role="presentation">
                               <tbody>
                                 <tr>
                                   <td><img alt="" src="%s" width="96" height="96" style="display:block;outline:none;border:none;text-decoration:none;margin:0 auto;margin-bottom:16px;border-radius:50%%" />
                                     <p style="font-size:20px;line-height:24px;margin:16px 0">%s</p><a href="%s" target="_blank" style="font-family:-apple-system,BlinkMacSystemFont,&quot;Segoe UI&quot;,Roboto,Oxygen-Sans,Ubuntu,Cantarell,&quot;Helvetica Neue&quot;,sans-serif;background-color:#8040ff;border-radius:3px;color:#fff;font-size:14px;text-decoration:none;text-align:center;p-x:20px;p-y:10px;line-height:100%%;display:inline-block;max-width:100%%;padding:10px 20px"><span><!--[if mso]><i style="letter-spacing: 20px;mso-font-width:-100%%;mso-text-raise:15" hidden>&nbsp;</i><![endif]--></span><span style="font-family:-apple-system,BlinkMacSystemFont,&quot;Segoe UI&quot;,Roboto,Oxygen-Sans,Ubuntu,Cantarell,&quot;Helvetica Neue&quot;,sans-serif;background-color:#8040ff;border-radius:3px;color:#fff;font-size:14px;text-decoration:none;text-align:center;p-x:20px;p-y:10px;max-width:100%%;display:inline-block;line-height:120%%;text-transform:none;mso-padding-alt:0px;mso-text-raise:7.5px">프로필 보기</span><span><!--[if mso]><i style="letter-spacing: 20px;mso-font-width:-100%%" hidden>&nbsp;</i><![endif]--></span></a>
                                   </td>
                                 </tr>
                               </tbody>
                             </table>
                             <p style="font-size:18px;line-height:1.2;margin:16px 0;font-family:-apple-system,BlinkMacSystemFont,&quot;Segoe UI&quot;,Roboto,Oxygen-Sans,Ubuntu,Cantarell,&quot;Helvetica Neue&quot;,sans-serif;color:#484848;padding:24px;background-color:#f2f3f3;border-radius:4px">%s</p><br />
                             <p style="font-size:18px;line-height:1.2;margin:16px 0;font-family:-apple-system,BlinkMacSystemFont,&quot;Segoe UI&quot;,Roboto,Oxygen-Sans,Ubuntu,Cantarell,&quot;Helvetica Neue&quot;,sans-serif;color:#484848">이 쪽지에 대한 답변은 <a target="_blank" style="color:#067df7;text-decoration:none" href="mailto:%s">%s</a>로 보내주세요.</p>
                             <hr style="width:100%%;border:none;border-top:1px solid #eaeaea" />
                             <table style="width:100%%" align="center" border="0" cellPadding="0" cellSpacing="0" role="presentation">
                               <tbody>
                                 <tr>
                                   <td><img alt="SOPT" src="https://playground.sopt.org/icons/logo/time=30.svg" width="90" height="30" style="display:block;outline:none;border:none;text-decoration:none;margin:20px auto 0 auto" /></td>
                                 </tr>
                               </tbody>
                             </table>
                           </div>
                           <div><!--[if mso | IE>
                           </td><td></td></tr></table>
                           <![endif]--></div>
                         </td>
                       </tr>
                     </tbody>
                   </table>
                 </html>
                """.formatted(sender, category, sender, category, profilePicUrl, sender, profileUrl, content, senderEmail, senderEmail);
    }
}
