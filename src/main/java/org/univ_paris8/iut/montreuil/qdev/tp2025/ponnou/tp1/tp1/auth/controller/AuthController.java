package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.dto.LoginDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.dto.RefreshRequestDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.dto.TokenDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.RefreshToken;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.security.JwtService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.service.RefreshTokenService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ErrorResponse;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentification stateless via JWT")
public class AuthController {

    private static final String UNAUTHORIZED = "UNAUTHORIZED";

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserService userService, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un JWT et un refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentification reussie"),
            @ApiResponse(responseCode = "400", description = "Requete invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> login(@Valid @RequestBody LoginDTO dto) {
        log.debug("Login attempt username={}", dto.getUsername());

        return userService.authenticate(dto.getUsername(), dto.getPassword())
                .<ResponseEntity<Object>>map(this::successResponse)
                .orElseGet(() -> unauthorized(dto.getUsername()));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renouveler le token", description = "Echange un refresh token valide contre un nouveau JWT et un nouveau refresh token (rotation).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renouvel\u00e9"),
            @ApiResponse(responseCode = "400", description = "Requete invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Refresh token invalide ou expire",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> refresh(@Valid @RequestBody RefreshRequestDTO dto) {
        return refreshTokenService.findByToken(dto.getRefreshToken())
                .filter(rt -> !rt.isRevoked() && !rt.isExpired())
                .<ResponseEntity<Object>>map(rt -> {
                    User user = rt.getUser();
                    String newJwt = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
                    log.info("Token refreshed username={}", user.getUsername());
                    return ResponseEntity.ok(new TokenDTO(newJwt, user.getUsername(), newRefreshToken.getToken()));
                })
                .orElseGet(() -> {
                    log.warn("Invalid or expired refresh token");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ErrorResponse(UNAUTHORIZED, "Refresh token invalide ou expire"));
                });
    }

    @PostMapping("/logout")
    @Operation(summary = "Deconnexion", description = "Invalide tous les refresh tokens de l'utilisateur.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deconnexion reussie"),
            @ApiResponse(responseCode = "401", description = "Refresh token invalide",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> logout(@Valid @RequestBody RefreshRequestDTO dto) {
        return refreshTokenService.findByToken(dto.getRefreshToken())
                .<ResponseEntity<Object>>map(rt -> {
                    refreshTokenService.revokeAllByUser(rt.getUser().getId());
                    log.info("Logout username={}", rt.getUser().getUsername());
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(UNAUTHORIZED, "Refresh token invalide")));
    }

    private ResponseEntity<Object> successResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        log.info("Login successful username={}", user.getUsername());
        return ResponseEntity.ok(new TokenDTO(token, user.getUsername(), refreshToken.getToken()));
    }

    private ResponseEntity<Object> unauthorized(String username) {
        log.warn("Login failed username={}", username);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(UNAUTHORIZED, "Identifiants invalides"));
    }
}
