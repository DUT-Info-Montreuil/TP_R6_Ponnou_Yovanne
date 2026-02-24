package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class RefreshRequestDTO {

    @NotBlank(message = "Le refresh token est obligatoire")
    @Schema(description = "Refresh token obtenu lors du login", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    public RefreshRequestDTO() {
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
