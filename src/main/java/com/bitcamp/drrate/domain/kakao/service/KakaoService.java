package com.bitcamp.drrate.domain.kakao.service;

import java.io.IOException;

import com.bitcamp.drrate.domain.users.dto.response.UsersResponseDTO.KakaoUserInfoDto;

import jakarta.servlet.http.HttpServletResponse;

public interface KakaoService {
    
    public void loginKakao(HttpServletResponse response) throws IOException;

    public KakaoUserInfoDto login(String code);
}
