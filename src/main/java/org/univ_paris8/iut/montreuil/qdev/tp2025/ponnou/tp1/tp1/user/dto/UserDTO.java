package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private Timestamp createdAt;
}
