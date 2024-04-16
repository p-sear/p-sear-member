package com.pser.member.config.oauth;


import com.pser.member.dto.OAuthUserDto;

public interface OAuthStrategy {
    String GOOGLE = "google";
    String KAKAO = "kakao";
    String NAVER = "naver";

    String getAccessToken(String code);

    OAuthUserDto getOAuthUser(String accessToken);

    String getStrategyCode();
}
