package com.pser.member.application;

import com.pser.member.config.TokenProvider;
import com.pser.member.config.oauth.GoogleStrategy;
import com.pser.member.config.oauth.OAuthStrategy;
import com.pser.member.dao.UserDao;
import com.pser.member.domain.User;
import com.pser.member.dto.OAuthUserDto;
import com.pser.member.exception.SignupFailedException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OAuthService {
    private final GoogleStrategy googleStrategy;
    private final UserDao userDao;
    private final TokenProvider tokenProvider;

    public String signupOrLoginByCode(String code, String oAuthType) {
        OAuthStrategy strategy = getStrategy(oAuthType);
        String oAuthAccessToken = strategy.getAccessToken(code);
        OAuthUserDto oAuthUserDto = strategy.getOAuthUser(oAuthAccessToken);
        return signupOrLogin(oAuthUserDto);
    }

    private String signupOrLogin(OAuthUserDto oAuthUserDto) {
        User user = getOrCreateUser(oAuthUserDto);
        return tokenProvider.createToken(user.getEmail());
    }

    private OAuthStrategy getStrategy(String oAuthType) {
        OAuthStrategy strategy;
        if (oAuthType.equals(OAuthStrategy.GOOGLE)) {
            strategy = googleStrategy;
        } else {
            throw new IllegalArgumentException("OAuth 타입 불명");
        }
        return strategy;
    }

    private User getOrCreateUser(OAuthUserDto oAuthUserDto) {
        Optional<User> user = userDao.findByEmail(oAuthUserDto.getEmail());
        if (user.isEmpty()) {
            return createUser(oAuthUserDto);
        }
        validateOAuthUserType(user.get(), oAuthUserDto);
        return user.get();
    }

    private User createUser(OAuthUserDto oAuthUserDto) {
        User user = User.builder()
                .email(oAuthUserDto.getEmail())
                .password(oAuthUserDto.getStrategyCode())
                .build();
        userDao.save(user);
        return user;
    }

    private void validateOAuthUserType(User user, OAuthUserDto oAuthUserDto) {
        String strategyCode = user.getPassword();
        if (!strategyCode.equals(oAuthUserDto.getStrategyCode())) {
            throw new SignupFailedException();
        }
    }
}
