package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto;

import java.sql.Timestamp;

public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private Timestamp createdAt;

    public UserDTO() {
    }

    private UserDTO(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.createdAt = builder.createdAt;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Timestamp getCreatedAt() { return createdAt; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String username;
        private String email;
        private Timestamp createdAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder createdAt(Timestamp createdAt) { this.createdAt = createdAt; return this; }

        public UserDTO build() {
            return new UserDTO(this);
        }
    }
}
