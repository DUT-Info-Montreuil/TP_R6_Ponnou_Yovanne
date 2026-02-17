package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ErrorResponse;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.dto.LoginDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.dto.TokenDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.jaas.UserPrincipal;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Slf4j
@Path("/login")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Auth", description = "Authentification stateless via JAAS")
public class AuthController {

    @POST
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur via JAAS et retourne un token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentification reussie", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Requete invalide", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content())
    })
    public Response login(@Valid LoginDTO dto) {
        log.debug("Login attempt username={}", dto.getUsername());

        try {
            LoginContext loginContext = new LoginContext("MasterAnnonceLogin", callbacks -> {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName(dto.getUsername());
                    } else if (callback instanceof PasswordCallback) {
                        ((PasswordCallback) callback).setPassword(dto.getPassword().toCharArray());
                    }
                }
            });

            loginContext.login();

            Subject subject = loginContext.getSubject();

            UserPrincipal userPrincipal = subject.getPrincipals(UserPrincipal.class).iterator().next();
            String username = userPrincipal.getUsername();

            String token = subject.getPublicCredentials(String.class).iterator().next();

            log.info("Login successful username={}", username);
            return Response.ok(new TokenDTO(token, username)).build();

        } catch (LoginException e) {
            log.warn("Login failed username={}: {}", dto.getUsername(), e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("UNAUTHORIZED", "Identifiants invalides"))
                    .build();
        }
    }
}
