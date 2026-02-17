package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.jaas;

import java.security.Principal;

public class UserPrincipal implements Principal {

    private final Long userId;
    private final String username;

    public UserPrincipal(String username, Long userId) {
        this.username = username;
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getName() {
        return username;
    }
}
