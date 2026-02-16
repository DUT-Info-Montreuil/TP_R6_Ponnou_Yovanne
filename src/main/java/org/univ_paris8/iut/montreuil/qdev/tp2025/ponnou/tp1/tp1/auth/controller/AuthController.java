package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ErrorResponse;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.service.UserService;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.dto.LoginDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.dto.TokenDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.TokenStore;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/login")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Auth", description = "Authentification stateless")
public class AuthController {

    private final UserService userService = new UserService();

    @POST
    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentification reussie", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Requete invalide", content = @Content()),
            @ApiResponse(responseCode = "401", description = "Identifiants invalides", content = @Content()),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content())
    })
    public Response login(@Valid LoginDTO dto) {
        Optional<User> user = userService.authenticate(dto.getUsername(), dto.getPassword());

        if (user.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse("UNAUTHORIZED", "Identifiants invalides"))
                    .build();
        }

        String token = TokenStore.generateToken(user.get().getId());
        return Response.ok(new TokenDTO(token, user.get().getUsername())).build();
    }
}
