package com.bitcamp.drrate.domain.users.service;


import com.bitcamp.drrate.domain.oauth.kakao.dto.response.KakaoUserInfoResponseDTO;
import com.bitcamp.drrate.domain.users.dto.CustomUserDetails;
import com.bitcamp.drrate.domain.users.dto.response.UsersResponseDTO;
import com.bitcamp.drrate.domain.users.entity.Users;
import com.bitcamp.drrate.domain.users.repository.UsersRepository;

import com.bitcamp.drrate.global.code.resultCode.ErrorStatus;
import com.bitcamp.drrate.global.exception.exceptionhandler.UsersServiceExceptionHandler;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.bitcamp.drrate.domain.users.dto.request.UsersRequestDTO.UsersJoinDTO;
import com.bitcamp.drrate.domain.users.entity.Role;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    @Override
    @Transactional
    public Long getUserId(CustomUserDetails user) {
        Long id = user.getId();
        Users users = usersRepository.findUsersById(id)
            .orElseThrow(() -> new UsersServiceExceptionHandler(ErrorStatus.USER_ID_CANNOT_FOUND));
        return users.getId();
    }

    @Override
    @Transactional
    public UsersResponseDTO.ChatRoomUserInfo getChatRoomUserInfo(Long userId) {
        Users users = usersRepository.findUsersById(userId)
                .orElseThrow(() -> new UsersServiceExceptionHandler(ErrorStatus.USER_ID_CANNOT_FOUND));
        return UsersResponseDTO.ChatRoomUserInfo.builder()
                .name(users.getUsername())
                .email(users.getEmail())
                .build();
    }

    @Override
    public Users handleLoginOrSignup(KakaoUserInfoResponseDTO userInfo) {
        //이메일로 기존 사용자 조회
        Optional<Users> existUsers = usersRepository.findByEmail(userInfo.getKakaoAccount().getEmail());

        if (existUsers.isPresent()) {
            //기존 사용자 로그인 처리
            return existUsers.get();
        }
        // 신규 사용자 회원가입 처리
        Users newUsers = Users.builder()
                .email(userInfo.getKakaoAccount().getEmail())
                .username(userInfo.getKakaoAccount().getProfile().getNickName())
                .build();

        return usersRepository.save(newUsers);
    }


    @Override //일반 사용자 회원가입
    public void joinProc(UsersJoinDTO joinDTO) {
        String username = joinDTO.getUsername();
        String userId = joinDTO.getUserId();
        String password = joinDTO.getPassword();
        String email = joinDTO.getEmail();

        Boolean isExist = usersRepository.existsByEmail(email);

        if(isExist) {
            return;
        }

        Users users = new Users();

        users.setUsername(username);
        users.setEmail(email);
        users.setRole(Role.USER);
        users.setPassword(bCryptPasswordEncoder.encode(password));
        users.setUserId(userId);

        usersRepository.save(users);
    }

    @Override // 소셜로그인으로 로그인 시 Header에 AccessToken 전달
    public HttpHeaders tokenSetting(String access) {
        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "Bearer " + access);
        return headers;
    }
}
