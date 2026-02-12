package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UserUpdateDTO {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email")
    private String email;

    public UserUpdateDTO() {
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
