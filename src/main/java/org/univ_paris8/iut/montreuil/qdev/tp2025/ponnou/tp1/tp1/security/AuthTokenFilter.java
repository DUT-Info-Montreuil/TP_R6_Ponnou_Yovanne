package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.security;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.dto.ErrorResponse;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Optional;

/**
 * Filtre JAX-RS stateless qui intercepte les endpoints annotes @Secured.
 * Verifie la presence et la validite du token dans le header :
 *   Authorization: Bearer <token>
 *
 * Si le token est absent ou invalide, retourne 401 Unauthorized.
 * Si le token est valide, injecte l'userId dans une propriete
 * du contexte de la requete pour que les resources puissent y acceder.
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthTokenFilter implements ContainerRequestFilter {

    public static final String USER_ID_PROPERTY = "userId";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            abortUnauthorized(requestContext, "Token manquant ou format invalide. Utilisez: Authorization: Bearer <token>");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        Optional<Long> userId = TokenStore.getUserId(token);

        if (userId.isEmpty()) {
            abortUnauthorized(requestContext, "Token invalide ou expiré");
            return;
        }

        // Rend l'userId disponible pour les resources via requestContext
        requestContext.setProperty(USER_ID_PROPERTY, userId.get());
    }

    private void abortUnauthorized(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(new ErrorResponse("UNAUTHORIZED", message))
                        .build()
        );
    }
}
