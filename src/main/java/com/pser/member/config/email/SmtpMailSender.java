package com.pser.member.config.email;

import com.pser.member.config.ThrottlingManager;
import com.pser.member.exception.MailServerException;
import com.pser.member.exception.RequestThrottlingException;
import java.io.UnsupportedEncodingException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmtpMailSender {
    private final JavaMailSender mailSender;
    private final ThrottlingManager throttlingManager = new ThrottlingManager();

    private final Environment env;

    public void sendEmail(String receiverAddress, String title, String content) {
        String senderName = "피서";
        String senderAddress = env.getProperty("mail.username");

        MimeMessage message = mailSender.createMimeMessage();//JavaMailSender 객체를 사용하여 MimeMessage 객체를 생성
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");//이메일 메시지와 관련된 설정을 수행합니다.
            // true를 전달하여 multipart 형식의 메시지를 지원하고, "utf-8"을 전달하여 문자 인코딩을 설정
            helper.setFrom(senderAddress, senderName);//이메일의 발신자 주소 설정
            helper.setTo(receiverAddress);//이메일의 수신자 주소 설정
            helper.setSubject(title);//이메일의 제목을 설정
            helper.setText(content, true);//이메일의 내용 설정 두 번째 매개 변수에 true를 설정하여 html 설정으로한다.
            if (throttlingManager.allowRequest()) {
                mailSender.send(message);
            } else {
                throw new RequestThrottlingException("Too many request");
            }

        } catch (MessagingException e) {
            throw new MailServerException();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("인코딩 오류 발생");
        }

    }

}
