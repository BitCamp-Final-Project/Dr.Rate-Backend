package com.bitcamp.drrate.domain.users.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bitcamp.drrate.domain.users.dto.CustomUserDetails;
import com.bitcamp.drrate.domain.users.entity.Users;
import com.bitcamp.drrate.domain.users.repository.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        
        Users users = usersRepository.findUsersById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + id));

        if(users != null) {
            return new CustomUserDetails(users);
        }

        return null;
    }
    
}
