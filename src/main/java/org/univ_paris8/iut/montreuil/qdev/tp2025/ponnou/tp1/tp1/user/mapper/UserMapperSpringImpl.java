package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.mapper;

import org.springframework.stereotype.Component;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserPatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.List;

@Component
public class UserMapperSpringImpl implements UserMapper {

    @Override
    public UserDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setRole(entity.getRole());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    @Override
    public List<UserDTO> toDTOList(List<User> entities) {
        return entities == null ? null : entities.stream().map(this::toDTO).toList();
    }

    @Override
    public void updateUserFromPatchDTO(UserPatchDTO dto, User entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getUsername() != null) {
            entity.setUsername(dto.getUsername());
        }
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
    }
}
