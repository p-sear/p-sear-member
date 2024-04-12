package com.example.member.config;

import com.example.member.Util;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@RequiredArgsConstructor
public class EmailConfig {
    private final Environment env;

    @Bean
    public JavaMailSender mailSender() {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(env.getProperty("mail.host"));// SMTP 서버 호스트 설정
        mailSender.setPort(Util.getIntProperty(env, "mail.port"));// 포트지정
        mailSender.setUsername(env.getProperty("mail.username"));//구글계정
        mailSender.setPassword(env.getProperty("mail.password"));//구글 앱 비밀번호

        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.transport.protocol", env.getProperty("mail.protocol"));//프로토콜로 smtp 사용
        javaMailProperties.put("mail.smtp.auth", env.getProperty("mail.smtp.auth"));//smtp 서버에 인증이 필요
        javaMailProperties.put("mail.smtp.socketFactory.class",
                env.getProperty("mail.socketFactory.class"));//SSL 소켓 팩토리 클래스 사용
        javaMailProperties.put("mail.smtp.starttls.enable",
                env.getProperty("mail.starttls.enable"));//STARTTLS(TLS를 시작하는 명령)를 사용하여 암호화된 통신을 활성화
        javaMailProperties.put("mail.debug", env.getProperty("mail.debug"));//디버깅 정보 출력
        javaMailProperties.put("mail.smtp.ssl.trust", env.getProperty("mail.ssl.trust"));//smtp 서버의 ssl 인증서를 신뢰
        javaMailProperties.put("mail.smtp.ssl.protocols", env.getProperty("mail.ssl.protocols"));//사용할 ssl 프로토콜 버젼

        mailSender.setJavaMailProperties(javaMailProperties);

        return mailSender;
    }
}