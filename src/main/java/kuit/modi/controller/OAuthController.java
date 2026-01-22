package kuit.modi.controller;

import jakarta.servlet.http.HttpServletResponse;
import kuit.modi.domain.CharacterType;
import kuit.modi.domain.Member;
import kuit.modi.dto.member.GoogleUserInfo;
import kuit.modi.exception.CustomException;
import kuit.modi.exception.MemberExceptionResponseStatus;
import kuit.modi.repository.CharacterTypeRepository;
import kuit.modi.repository.MemberRepository;
import kuit.modi.service.GoogleOAuthService;
import kuit.modi.service.JwtService;
import kuit.modi.service.TempTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth2")
public class OAuthController {
    private final GoogleOAuthService googleOAuthService;
    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final CharacterTypeRepository characterTypeRepository;
    private final TempTokenStore tempTokenStore;

    @Value("${app.mode}")
    private String mode;

    @Value("${app.frontend.local}")
    private String localRedirectBase;

    @Value("${app.frontend.prod}")
    private String prodRedirectBase;

    @GetMapping("/authorize/google")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        String redirectUrl = googleOAuthService.getGoogleLoginUrl();
        response.sendRedirect(redirectUrl);
    }

    @GetMapping("/callback/google")
    public void handleGoogleCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        String accessToken = googleOAuthService.getAccessToken(code);
        GoogleUserInfo userInfo = googleOAuthService.getUserInfo(accessToken);
        Optional<Member> existingMemberOpt = memberRepository.findByEmail(userInfo.email());

        boolean isNew = false;
        Member member;

        if (existingMemberOpt.isPresent()) {
            member = existingMemberOpt.get();
        } else {
            // 신규 회원이면 임시 회원 생성
            isNew = true;
            CharacterType momo = characterTypeRepository.findByName("momo")
                    .orElseThrow(() -> new CustomException(MemberExceptionResponseStatus.INVALID_CHARACTER_TYPE));

            Member newMember = new Member(userInfo.email(), momo);
            member = memberRepository.save(newMember);
        }

        // JWT 생성
        String jwt = jwtService.createToken(member.getId());
        System.out.println(jwt);

        /* 기존 토큰 임시 저장 코드
        String key = UUID.randomUUID().toString();
        tempTokenStore.save(key, jwt);
        */

        // 기존 쿠키 삭제 (만료시킴)
        ResponseCookie deleteCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0) // 즉시 만료
                .build();
        response.addHeader("Set-Cookie", deleteCookie.toString());

        // 새 쿠키 발급
        ResponseCookie newCookie = ResponseCookie.from("access_token", jwt)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofHours(24))
                .build();
        response.addHeader("Set-Cookie", newCookie.toString());

        // 환경에 따른 분기 처리 및 303 리다이렉트
        boolean isLocal = mode.equalsIgnoreCase("local");
        String baseRedirect = isLocal ? localRedirectBase : prodRedirectBase;
        String redirectUrl = baseRedirect + (isNew ? "/information-setting" : "/home");

        response.setStatus(HttpServletResponse.SC_SEE_OTHER);
        response.setHeader(HttpHeaders.LOCATION, redirectUrl);

        // 기존 302 리다이렉트
        //response.sendRedirect(redirectUrl);
    }

    // 사용 안 함
    @PostMapping("/set-cookie")
    public ResponseEntity<Void> setCookie(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String code = body.get("code");
        String jwt = tempTokenStore.get(code);

        if (jwt == null) {
            return ResponseEntity.badRequest().build();
        }

        // 기존 쿠키 삭제 (만료시킴)
        ResponseCookie deleteCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(0) // 즉시 만료
                .build();
        response.addHeader("Set-Cookie", deleteCookie.toString());

        // 새 쿠키 발급
        ResponseCookie newCookie = ResponseCookie.from("access_token", jwt)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .maxAge(Duration.ofHours(24))
                .build();
        response.addHeader("Set-Cookie", newCookie.toString());

        return ResponseEntity.ok().build();
    }
}
