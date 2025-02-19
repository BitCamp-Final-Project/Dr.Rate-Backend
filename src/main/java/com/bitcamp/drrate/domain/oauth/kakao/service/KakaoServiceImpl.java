package com.bitcamp.drrate.domain.oauth.kakao.service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.bitcamp.drrate.domain.jwt.JWTUtil;
import com.bitcamp.drrate.domain.jwt.refresh.RefreshTokenService;
import com.bitcamp.drrate.domain.oauth.kakao.dto.response.KakaoTokenResponseDTO;
import com.bitcamp.drrate.domain.oauth.kakao.dto.response.KakaoUserInfoResponseDTO;
import com.bitcamp.drrate.domain.users.entity.Role;
import static com.bitcamp.drrate.domain.users.entity.Role.ADMIN;
import com.bitcamp.drrate.domain.users.entity.Users;
import com.bitcamp.drrate.domain.users.repository.UsersRepository;
import com.bitcamp.drrate.global.code.resultCode.ErrorStatus;
import com.bitcamp.drrate.global.exception.exceptionhandler.UsersServiceExceptionHandler;

import io.netty.handler.codec.http.HttpHeaderValues;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoServiceImpl implements KakaoService {

    private final UsersRepository usersRepository;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final RedisTemplate<String, String> redisTemplate;

    private final String KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
    private final String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String client_id;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirect_uri;

    @Override
    public void loginKakao(HttpServletResponse response) throws IOException {
        try {
            String location = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" + client_id +"&redirect_uri=" + redirect_uri;

            response.sendRedirect(location);
        } catch (IOException e) {
            throw new UsersServiceExceptionHandler(ErrorStatus.SOCIAL_URL_NOT_FOUND);
        }
    }

    @Override
    public String login(String code) {
        try {
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
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        return Mono.error(new UsersServiceExceptionHandler(ErrorStatus.SOCIAL_PARAMETERS_INVALID));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                        return Mono.error(new UsersServiceExceptionHandler(ErrorStatus.INTERNAL_SERVER_ERROR));
                    })
                    .bodyToMono(KakaoTokenResponseDTO.class)
                    .block();

            if (kakaoTokenResponseDTO == null) {
                throw new UsersServiceExceptionHandler(ErrorStatus.SESSION_ACCESS_PARSE_ERROR);
            }
            KakaoUserInfoResponseDTO userInfo = getUserInfo(kakaoTokenResponseDTO.getAccessToken());

            //소셜로그인으로 들어올 시 해당하는 소셜의 정보가 바뀔 수 있기 때문에 업데이트를 계속 해주어야한다.
            String email = userInfo.getKakao_account().getEmail();

            Optional<Users> optionalUsers = usersRepository.findByEmail(email);
            boolean isNewUser = optionalUsers.isEmpty(); // 신규 가입자 여부 판단

            Users users = optionalUsers.orElseGet(() -> new Users());

            setUserInfo(users, userInfo);

            users.setSocial("Kakao");

            usersRepository.save(users);

            if(users.getId() == null) {
                Optional<Users> newUsers = usersRepository.findByEmail(email);
                users = newUsers.orElseGet(() -> new Users());
            }
            Long id = users.getId();

            // 신규 가입자일 경우 Redis 카운트 증가
            if (isNewUser) {
                incrementNewUserCount();
            }

            String access = null; String refresh = null;

            System.out.println("role : "+users.getRole());
            if(users.getRole().equals(ADMIN)){
                access = jwtUtil.createJwt(id, "access", "ROLE_ADMIN", 86400000L);
                refresh = jwtUtil.createJwt(id, "refresh", "ROLE_ADMIN", 86400000L);
            }else{
                access = jwtUtil.createJwt(id, "access", "ROLE_USER", 86400000L);
                refresh = jwtUtil.createJwt(id, "refresh", "ROLE_USER", 86400000L);
            }

            /* 우리 서버 token 값 */
            System.out.println("우리 서버 accessToken :  "+access);
            System.out.println("우리 서버 refreshToken :  "+refresh);

            /* 로그인 후 Redis에 access, refresh */
            refreshTokenService.saveTokens(String.valueOf(users.getId()), access, refresh);

            System.out.println(refreshTokenService.getAccessToken(String.valueOf(users.getId())));
            System.out.println(refreshTokenService.getRefreshToken(String.valueOf(users.getId())));

            /* 로그인 후 Redis에 access, refresh */
            refreshTokenService.saveTokens(String.valueOf(users.getId()), access, refresh);

            return access;
        } catch (UsersServiceExceptionHandler e) {
            throw new UsersServiceExceptionHandler(ErrorStatus.USER_LOGIN_ERROR);
        } catch (IOException ex) {
            throw new UsersServiceExceptionHandler(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }


    //사용자 정보 요청
    private KakaoUserInfoResponseDTO getUserInfo(String accessToken) throws IOException {
        try {
            return WebClient.create(KAUTH_USER_URL_HOST)
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .path("/v2/user/me")
                            .build(true))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // Access Token 인가
                    .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        log.error("[Kakao Service] Invalid request parameters for user info");
                        return Mono.error(new UsersServiceExceptionHandler(ErrorStatus.SESSION_ACCESS_PARSE_ERROR));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                        log.error("[Kakao Service] Internal server error during user info retrieval");
                        return Mono.error(new UsersServiceExceptionHandler(ErrorStatus.INTERNAL_SERVER_ERROR));
                    })
                    .bodyToMono(KakaoUserInfoResponseDTO.class)
                    .block();
        } catch (Exception e) {
            throw new UsersServiceExceptionHandler(ErrorStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void setUserInfo(Users users, KakaoUserInfoResponseDTO userInfo) {
        users.setEmail(userInfo.getKakao_account().getEmail());
        if(userInfo.getKakao_account().getName()!=null){
            users.setUsername(userInfo.getKakao_account().getName());
        } else if (userInfo.getKakao_account().getProfile().getNickName() != null) {
            users.setUsername(userInfo.getKakao_account().getProfile().getNickName());
        }else{
            users.setUsername("닉네임");
        }

        if(users.getRole() == Role.ADMIN){
            //if(users.getRole() == Role.ADMIN)
            users.setRole(Role.ADMIN);
        }else{
            users.setRole(Role.USER);
        }
    }
    private void incrementNewUserCount() {
        String today = LocalDate.now().toString();
        String redisKey = "daily_new_members:" + today;

        redisTemplate.opsForSet().add(redisKey, "new_member_" + UUID.randomUUID()); // 더미 값 추가
        redisTemplate.expire(redisKey, Duration.ofDays(1));
    }
}