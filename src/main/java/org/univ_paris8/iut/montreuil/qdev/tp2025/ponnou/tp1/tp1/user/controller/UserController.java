package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.dto.PaginatedResponse;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ResourceNotFoundException;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserCreateDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserPatchDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.dto.UserUpdateDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.mapper.UserMapper;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service.UserService;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gestion des utilisateurs")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    @Operation(summary = "Lister les utilisateurs")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des utilisateurs retournee")
    })
    public ResponseEntity<PaginatedResponse<UserDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<User> users = userService.findAllPaginated(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(new PaginatedResponse<>(
                users.map(userMapper::toDTO).getContent(),
                page, size, users.getTotalElements()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Recuperer un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur retourne"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouve")
    })
    public ResponseEntity<UserDTO> getById(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve avec l'id : " + id));
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    @PostMapping
    @Operation(summary = "Creer un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Utilisateur cree"),
            @ApiResponse(responseCode = "400", description = "Requete invalide")
    })
    public ResponseEntity<UserDTO> create(@Valid @RequestBody UserCreateDTO dto) {
        User created = userService.create(dto.getUsername(), dto.getEmail(), dto.getPassword());
        UserDTO responseDTO = userMapper.toDTO(created);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(responseDTO);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Mettre a jour un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur mis a jour"),
            @ApiResponse(responseCode = "400", description = "Requete invalide"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouve")
    })
    public ResponseEntity<UserDTO> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        User updated = userService.update(id, dto.getUsername(), dto.getEmail());
        return ResponseEntity.ok(userMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Supprimer un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Utilisateur supprime"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouve")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Patch partiel d'un utilisateur")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Utilisateur mis a jour"),
            @ApiResponse(responseCode = "400", description = "Requete invalide"),
            @ApiResponse(responseCode = "404", description = "Utilisateur non trouve")
    })
    public ResponseEntity<UserDTO> patch(@PathVariable Long id, @Valid @RequestBody UserPatchDTO dto) {
        User patched = userService.patch(id, dto);
        return ResponseEntity.ok(userMapper.toDTO(patched));
    }
}
