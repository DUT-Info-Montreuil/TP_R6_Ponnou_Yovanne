package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.dto.LoginDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.dto.TokenDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.JwtService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ErrorResponse;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentification stateless via JWT")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentification reussie", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Requete invalide", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides", content = @Content())
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto) {
        log.debug("Login attempt username={}", dto.getUsername());

        return userService.authenticate(dto.getUsername(), dto.getPassword())
                .<ResponseEntity<?>>map(this::successResponse)
                .orElseGet(() -> unauthorized(dto.getUsername()));
    }

    private ResponseEntity<TokenDTO> successResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
        log.info("Login successful username={}", user.getUsername());
        return ResponseEntity.ok(new TokenDTO(token, user.getUsername()));
    }

    private ResponseEntity<ErrorResponse> unauthorized(String username) {
        log.warn("Login failed username={}", username);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("UNAUTHORIZED", "Identifiants invalides"));
    }
}
