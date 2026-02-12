package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.filter;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ErrorResponse;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.Secured;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.TokenStore;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Optional;

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
