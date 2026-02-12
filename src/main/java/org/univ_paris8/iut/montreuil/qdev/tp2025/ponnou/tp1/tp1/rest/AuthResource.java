package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.rest;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dto.ErrorResponse;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dto.LoginDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dto.TokenDTO;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.model.User;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.security.TokenStore;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.service.UserService;

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
public class AuthResource {

    private final UserService userService = new UserService();

    /**
     * POST /api/login
     * Authentifie un utilisateur et retourne un token.
     * Le client doit envoyer ce token dans le header Authorization
     * pour acceder aux endpoints proteges :
     *   Authorization: Bearer <token>
     */
    @POST
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
