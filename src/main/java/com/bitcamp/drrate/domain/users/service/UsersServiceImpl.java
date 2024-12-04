package com.bitcamp.drrate.domain.users.service;


import com.bitcamp.drrate.domain.kakao.dto.response.KakaoUserInfoResponseDTO;
import com.bitcamp.drrate.domain.users.entity.Users;
import com.bitcamp.drrate.domain.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl implements UsersService {

    private final UsersRepository usersRepository;

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
                .name(userInfo.getKakaoAccount().getProfile().getNickName())
                // .profileImageUrl(userInfo.getKakaoAccount().getProfile().getProfileImageUrl())
                // .oauth("kakao")//제공자를 카카오로 설정
                .build();

        return usersRepository.save(newUsers);
    }
}
