package com.bitcamp.drrate.domain.users.dto;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bitcamp.drrate.domain.users.entity.Role;
import com.bitcamp.drrate.domain.users.entity.Users;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUserDetails extends Users implements UserDetails {
 
    private final Users users;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();
        Role role = users.getRole();
        if (role == Role.ADMIN) {
                collection.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            } else {
                collection.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
        return collection;
    }
    
    @Override
    public String getPassword() {
        return users.getPassword();
    }

    @Override
    public String getUsername() {
        return users.getUsername();
    }

    @Override
    public Long getId() {
        return users.getId();
    }

    @Override
    public String getUserId() {
        return users.getUserId();
    }

    @Override
    public String getEmail() {
        return users.getEmail();
    }
    
    @Override
    public String getSocial() {
        return users.getSocial();
    }
    
    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }

    @Override
    public boolean isEnabled() {

        return true;
    }
}
