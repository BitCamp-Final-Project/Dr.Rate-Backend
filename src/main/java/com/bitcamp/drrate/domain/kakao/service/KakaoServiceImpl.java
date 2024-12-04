package com.bitcamp.drrate.domain.kakao.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.bitcamp.drrate.domain.kakao.dto.response.KakaoTokenResponseDTO;
import com.bitcamp.drrate.domain.kakao.dto.response.KakaoUserInfoResponseDTO;
import com.bitcamp.drrate.domain.users.dto.response.UsersResponseDTO;
import com.bitcamp.drrate.domain.users.dto.response.UsersResponseDTO.KakaoUserInfoDto;

import io.netty.handler.codec.http.HttpHeaderValues;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoServiceImpl implements KakaoService {

    private final String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private final String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String client_id;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirect_uri;

    @Override
    public void loginKakao(HttpServletResponse response) throws IOException {
        String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + client_id +"&redirect_uri=" + redirect_uri;

        response.sendRedirect(location);
    }

    @Override
    public KakaoUserInfoDto login(String code) {
        KakaoTokenResponseDTO kakaoTokenResponseDTO = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/oauth/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", client_id)
                        .queryParam("code",code)
                        .build(true))
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invaild Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoTokenResponseDTO.class)
                .block();

        if(kakaoTokenResponseDTO == null) {
            log.error("[Kakao Service] Token response DTO is null");
            return null;
        }
        log.info("[Kakao Service] Access token ------> {}", kakaoTokenResponseDTO.getAccessToken());
        log.info("[Kakao Service] Refresh token ------> {}", kakaoTokenResponseDTO.getRefreshToken());
        //제공 조건: OpenID Connect가 활성화 된 앱의 토큰 발급 요청인 경우 또는 scope에 openid를 포함한 추가 항목 동의 받기 요청을 거친 토큰 발급 요청인 경우
        log.info(" [Kakao Service] Id Token ------> {}", kakaoTokenResponseDTO.getIdToken());
        log.info(" [Kakao Service] Scope ------> {}", kakaoTokenResponseDTO.getScope());

        return getUserInfo(kakaoTokenResponseDTO.getAccessToken());
    }

    //사용자 정보 요청
    private KakaoUserInfoDto getUserInfo(String accessToken) {
        KakaoUserInfoResponseDTO userInfo = WebClient.create(KAUTH_USER_URL_HOST)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) //access token 인가
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoUserInfoResponseDTO.class)
                .block();

        if(userInfo == null) {
            log.error("[Kakao Service] UserInfo is null");
            return null;
        }
        log.info("[Kakao Service] Auth ID ---> {} ", userInfo.getId());
        log.info("[Kakao Service] NickName ---> {} ", userInfo.getKakaoAccount().getProfile().getNickName());
        log.info("[Kakao Service] ProfileImageUrl ---> {} ", userInfo.getKakaoAccount().getProfile().getProfileImageUrl());

        String name = userInfo.getKakaoAccount().getName();
        String email = userInfo.getKakaoAccount().getEmail();

        KakaoUserInfoDto userDTO = new KakaoUserInfoDto();
        userDTO.setName(name);
        userDTO.setEmail(email);


        return userDTO;
    }
}
