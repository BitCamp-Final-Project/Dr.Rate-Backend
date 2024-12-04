package com.bitcamp.drrate.domain.users.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.bitcamp.drrate.domain.google.service.GoogleService;
import com.bitcamp.drrate.domain.kakao.service.KakaoService;
import com.bitcamp.drrate.domain.users.dto.response.UsersResponseDTO.GoogleUserInfoDto;
import com.bitcamp.drrate.domain.users.dto.response.UsersResponseDTO.KakaoUserInfoDto;
import com.bitcamp.drrate.domain.users.entity.Users;
import com.bitcamp.drrate.domain.users.repository.UsersRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UsersController {

    private final GoogleService googleService;
    private final KakaoService kakaoService;
    private final UsersRepository usersRepository;

    @GetMapping("/login/{provider}")
    public void userLogin(HttpServletResponse response, @PathVariable("provider") String provider) throws IOException {
        if(provider.equals("google")){
            googleService.loginGoogle(response);
        }
        else if(provider.equals("kakao")){
            kakaoService.loginKakao(response);
        }
    }

    @GetMapping("/login/oauth2/code/{provider}")
    public String login(@RequestParam("code") String code, @PathVariable("provider") String provider) {
        if(provider.equals("google")){
            GoogleUserInfoDto userDTO = googleService.login(code);
            String email = userDTO.getEmail();
            String name = userDTO.getName();
            if(usersRepository.findByEmail(userDTO.getEmail()) == null) { // 소셜로그인으로 처음 들어오는 유저를 거름
                // return "redirect:" + "추가적인 정보를 받는 페이지로 리다이렉트시킴"; // 추가로 받는 정보는 생년월일 추가하면 이름정도 이동할 땐 받아온 유저정보를 받아서 같이보냄
                return "redirect:" + "/addInfo?email=" + email + "&name=" + name;
            } else return "redirect:" + "mainPage"; // 정보가 있으면 로그인 컨트롤러로 보내서 JWT토큰 발급
        }
        else if(provider.equals("kakao")){
            KakaoUserInfoDto userDTO = kakaoService.login(code);
            String email = userDTO.getEmail();
            String name = userDTO.getName();
            if(usersRepository.findByEmail(userDTO.getEmail()) == null) { // 소셜로그인으로 처음 들어오는 유저를 거름
                // return "redirect:" + "추가적인 정보를 받는 페이지로 리다이렉트시킴";
                return "redirect:" + "/addInfo?email=" + email + "&name=" + name;
            } else return "redirect:" + "mainPage"; // 정보가 있으면 로그인 컨트롤러로 보내서 JWT토큰 발급
        }
        else return "rediret:" + "mainPage"; // 정보가 있으면 로그인 컨트롤러로 보내서 JWT토큰 발급
    }
    // 소셜로그인으로 처음 접속하는 사용자의 정보를 DB에 저장하는 컨트롤러
    @GetMapping("/addInfo") 
    public String addInfo(@RequestParam String email, @RequestParam String name, Model model) {
        model.addAttribute("email", email);
        model.addAttribute("name", name);
        return "redirect:" + "정보들고 수정페이지 이동";
    }

    // 사용자 정보 수정
    @GetMapping("/updateUser")
    public String updateUser(@RequestBody Users users) {
        usersRepository.save(users);
        return "redirect:" + "mainPage";
    }
    
}