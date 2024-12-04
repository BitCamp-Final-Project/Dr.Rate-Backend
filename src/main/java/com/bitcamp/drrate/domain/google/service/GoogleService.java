package com.bitcamp.drrate.domain.google.service;

import java.io.IOException;

import com.bitcamp.drrate.domain.users.dto.response.UsersResponseDTO.GoogleUserInfoDto;

import jakarta.servlet.http.HttpServletResponse;

public interface GoogleService {

    public void loginGoogle(HttpServletResponse response) throws IOException;

    public GoogleUserInfoDto login(String code);

}
