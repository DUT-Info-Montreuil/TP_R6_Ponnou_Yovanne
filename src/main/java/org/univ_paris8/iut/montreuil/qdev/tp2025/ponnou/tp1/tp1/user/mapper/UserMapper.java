package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.mapper;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDTO toDTO(User entity) {
        return UserDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static List<UserDTO> toDTOList(List<User> entities) {
        return entities.stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
    }
}
