package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dto;

import javax.validation.constraints.NotBlank;

public class LoginDTO {

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;

    public LoginDTO() {
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
