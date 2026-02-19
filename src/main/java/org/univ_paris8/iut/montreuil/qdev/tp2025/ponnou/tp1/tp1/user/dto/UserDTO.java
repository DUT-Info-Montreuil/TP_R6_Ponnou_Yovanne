package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {

    @Schema(description = "Identifiant utilisateur", example = "7")
    private Long id;
    @Schema(description = "Nom d'utilisateur", example = "yovanne")
    private String username;
    @Schema(description = "Adresse email", example = "yovanne@example.com")
    private String email;
    @Schema(description = "Role applicatif", example = "ROLE_USER")
    private String role;
    @Schema(description = "Date de creation", example = "2026-02-19T10:30:00.000+00:00")
    private Timestamp createdAt;
}
