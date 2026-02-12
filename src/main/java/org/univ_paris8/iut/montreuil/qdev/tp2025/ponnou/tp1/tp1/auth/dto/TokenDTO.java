package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.dto;

public class TokenDTO {

    private String token;
    private String username;

    public TokenDTO() {
    }

    public TokenDTO(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
