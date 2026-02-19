package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public class AuthenticatedUser {

    private final Long userId;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedUser(Long userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.authorities = List.of(new SimpleGrantedAuthority(role));
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
