package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class TokenDTO {

    @Schema(description = "JWT Bearer token", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiJ9.signature")
    private String token;
    @Schema(description = "Nom d'utilisateur authentifie", example = "admin")
    private String username;
    @Schema(description = "Refresh token pour renouveler le JWT", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    public TokenDTO() {
    }

    public TokenDTO(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public TokenDTO(String token, String username, String refreshToken) {
        this.token = token;
        this.username = username;
        this.refreshToken = refreshToken;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
