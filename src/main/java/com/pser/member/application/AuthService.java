package com.pser.member.application;

import com.pser.member.config.TokenProvider;
import com.pser.member.config.email.SmtpMailSender;
import com.pser.member.dao.UserDao;
import com.pser.member.domain.User;
import com.pser.member.dto.LoginRequest;
import com.pser.member.dto.SendMailRequest;
import com.pser.member.dto.SignupRequest;
import com.pser.member.dto.TokenResponse;
import com.pser.member.exception.LoginFailedException;
import com.pser.member.exception.UserAlreadyExistsException;
import com.pser.member.exception.UserNotAllowedException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final SmtpMailSender smtpMailSender;
    private final RedisTemplate<String, Object> redisTemplate;

    public void signup(SignupRequest request) {
        if (!confirmMail(request.getEmail(), request.getEmailCode())) {
            throw new UserNotAllowedException("인증되지 않은 사용자입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        request.setPassword(encodedPassword);

        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        userDao.save(user);
    }

    public TokenResponse authenticate(LoginRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authentication;
        try {
            authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            throw new LoginFailedException("비밀번호가 일치하지 않습니다");
        } catch (InternalAuthenticationServiceException e) {
            throw new LoginFailedException("존재하지 않는 유저입니다");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return TokenResponse.builder()
                .token(tokenProvider.createToken(authentication, false))
                .refreshToken(tokenProvider.createToken(authentication, true))
                .build();
    }

    public String refresh(String refreshToken) {
        return tokenProvider.refresh(refreshToken);
    }

    public String createEmailAuthCode() {
        Random rnd = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(rnd.nextInt(10));
        }
        return code.toString();
    }

    public void sendEmail(SendMailRequest request) {
        if (userDao.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException();
        }

        String authCode = createEmailAuthCode();

        String title = "회원 가입 인증 이메일 입니다.";
        String content =
                "<h1>환영합니다.</h1>" +
                        "<br><br>" +
                        "인증 번호는 " + authCode + "입니다." +
                        "<br>" +
                        "인증번호를 사이트에 입력해주세요.";

        smtpMailSender.sendEmail(request.getEmail(), title, content);

        redisTemplate.opsForValue().set(request.getEmail(), authCode, 5, TimeUnit.MINUTES);
    }

    public boolean confirmMail(String email, String emailCode) {
        String authCode = (String) redisTemplate.opsForValue().get(email);
        redisTemplate.delete(email);
        return emailCode.equals(authCode);
    }

    public Boolean confirmMailAndRefresh(String email, String emailCode) {
        boolean isValid = confirmMail(email, emailCode);
        if (isValid) {
            redisTemplate.opsForValue().set(email, emailCode, 1, TimeUnit.HOURS);
        }
        return isValid;
    }
}
