package com.pser.member.config.oauth;

import com.pser.member.dto.OAuthUserDto;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GoogleStrategy implements OAuthStrategy {
    private final Environment env;
    private final RestTemplate restTemplate;

    @Override
    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("code", code);
        requestBody.put("client_id", env.getProperty("oauth.google.client-id"));
        requestBody.put("client_secret", env.getProperty("oauth.google.client-secret"));
        requestBody.put("redirect_uri", env.getProperty("oauth.google.redirect-uri"));
        requestBody.put("grant_type", "authorization_code");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        Map response = restTemplate.postForObject(
                env.getProperty("oauth.google.token-uri", ""),
                request,
                Map.class
        );

        return (String) response.get("access_token");
    }

    @Override
    public OAuthUserDto getOAuthUser(String accessToken) {
        String url = "%s?access_token=%s".formatted(env.getProperty("oauth.google.user-info-uri"), accessToken);
        Map response = restTemplate.getForObject(url, Map.class);
        return OAuthUserDto.builder()
                .email((String) response.get("email"))
                .strategyCode(getStrategyCode())
                .build();
    }

    @Override
    public String getStrategyCode() {
        return OAuthStrategy.GOOGLE;
    }
}
