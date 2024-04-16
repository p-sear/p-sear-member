package com.pser.member.api;

import com.pser.member.application.AuthService;
import com.pser.member.application.OAuthService;
import com.pser.member.common.response.ApiResponse;
import com.pser.member.dto.AuthenticateResponse;
import com.pser.member.dto.ConfirmMailRequest;
import com.pser.member.dto.LoginRequest;
import com.pser.member.dto.SendMailRequest;
import com.pser.member.dto.SignupRequest;
import com.pser.member.dto.UserinfoResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class AuthApi {
    private final AuthService authService;
    private final OAuthService oAuthService;
    private final Environment env;


    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody @Validated SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<AuthenticateResponse>> authenticate(
            @RequestBody @Validated LoginRequest request) {
        AuthenticateResponse result = authService.authenticate(request);
        return ResponseEntity.ok().body(ApiResponse.success(result));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<String>> refresh(@RequestBody String refreshToken) {
        String result = authService.refresh(refreshToken);
        return ResponseEntity.ok().body(ApiResponse.success(result));
    }

    @GetMapping("/userinfo")
    public ResponseEntity<ApiResponse<UserinfoResponse>> getUserinfo(
            @RequestHeader("User-Id") String userId,
            @RequestHeader("User-Email") String userEmail,
            @RequestHeader("User-Role") String userRole
    ) {
        UserinfoResponse result = UserinfoResponse.builder()
                .id(Long.parseLong(userId))
                .email(userEmail)
                .role(userRole)
                .build();
        return ResponseEntity.ok().body(ApiResponse.success(result));
    }

    @PostMapping("/send-mail")
    public ResponseEntity<ApiResponse<String>> sendMail(@RequestBody @Validated SendMailRequest request,
                                                        HttpServletRequest servletRequest) {
        authService.sendEmail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-mail")
    public ResponseEntity<ApiResponse<Boolean>> confirmMail(@RequestBody @Validated ConfirmMailRequest request) {
        Boolean isCodeMatching = authService.confirmMail(request.getEmail(), request.getEmailCode());
        return ResponseEntity.ok().body(ApiResponse.success(isCodeMatching));
    }

    @GetMapping("/oauth-types/{oAuthType}/validate-oauth2-code")
    public void validateOAuth2Code(
            @RequestParam String code,
            @PathVariable String oAuthType,
            HttpServletResponse response
    ) throws IOException {
        String redirectUrl = env.getProperty("oauth.front-uri");
        try {
            String token = oAuthService.signupOrLoginByCode(code, oAuthType);
            response.sendRedirect("%s?token=%s".formatted(redirectUrl, token));
        } catch (Exception e) {
            String encodedErrorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect("%s?error=%s".formatted(redirectUrl, encodedErrorMessage));
        }
    }
}
