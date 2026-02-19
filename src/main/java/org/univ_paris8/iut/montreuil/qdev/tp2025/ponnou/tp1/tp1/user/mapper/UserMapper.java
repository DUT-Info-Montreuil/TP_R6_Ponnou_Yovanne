package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserPatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User entity);

    List<UserDTO> toDTOList(List<User> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "annonces", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUserFromPatchDTO(UserPatchDTO dto, @MappingTarget User entity);
}
