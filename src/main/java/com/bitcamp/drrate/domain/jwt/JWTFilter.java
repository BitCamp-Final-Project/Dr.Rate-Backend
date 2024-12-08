package com.bitcamp.drrate.domain.jwt;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bitcamp.drrate.domain.users.dto.CustomUserDetails;
import com.bitcamp.drrate.domain.users.entity.Role;
import com.bitcamp.drrate.domain.users.entity.Users;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String accessToken = request.getHeader("authorization");

        // 토큰이 없으면 다음 필터로 넘김
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        accessToken = accessToken.substring(7); // Remove "Bearer " prefix
        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try{
            jwtUtil.isExpired(accessToken);
        } catch(ExpiredJwtException e) {
            //response body
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");
            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } // try/catch
        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);

        if(!category.equals("access")) {
            //response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } // if
        String userId = jwtUtil.getUserId(accessToken);
        String role = jwtUtil.getRole(accessToken);

        Users users = new Users();
        users.setUserId(userId);
        if(role.equals("ADMIN")){
            users.setRole(Role.ADMIN);
        } else {
            users.setRole(Role.USER);
        }
        CustomUserDetails customUserDetails = new CustomUserDetails(users);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
