package com.psear.member.application;

import com.psear.member.config.TokenProvider;
import com.psear.member.config.oauth.GoogleStrategy;
import com.psear.member.config.oauth.OAuthStrategy;
import com.psear.member.dao.UserDao;
import com.psear.member.domain.User;
import com.psear.member.dto.OAuthUserDto;
import com.psear.member.exception.SignupFailedException;
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
