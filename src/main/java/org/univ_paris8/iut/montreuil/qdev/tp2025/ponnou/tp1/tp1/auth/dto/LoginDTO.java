package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class LoginDTO {

    @Schema(description = "Nom d'utilisateur", example = "admin")
    @NotBlank(message = "username is required")
    private String username;

    @Schema(description = "Mot de passe", example = "admin123")
    @NotBlank(message = "password is required")
    private String password;

    public LoginDTO() {
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
