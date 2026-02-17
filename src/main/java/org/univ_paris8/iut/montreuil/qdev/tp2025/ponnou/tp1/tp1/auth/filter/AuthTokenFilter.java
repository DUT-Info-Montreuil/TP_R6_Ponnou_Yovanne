package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.filter;

import lombok.extern.slf4j.Slf4j;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception.ErrorResponse;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.Secured;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.jaas.RolePrincipal;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.jaas.UserPrincipal;

import javax.annotation.Priority;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.security.Principal;
import java.util.Set;

@Slf4j
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

        try {
            LoginContext loginContext = new LoginContext("MasterAnnonceToken", (Callback[] callbacks) -> {
                for (Callback callback : callbacks) {
                    if (callback instanceof NameCallback) {
                        ((NameCallback) callback).setName(token);
                    }
                }
            });

            loginContext.login();

            Subject subject = loginContext.getSubject();
            UserPrincipal userPrincipal = subject.getPrincipals(UserPrincipal.class).iterator().next();
            Set<RolePrincipal> roles = subject.getPrincipals(RolePrincipal.class);

            requestContext.setProperty(USER_ID_PROPERTY, userPrincipal.getUserId());

            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return userPrincipal;
                }

                @Override
                public boolean isUserInRole(String role) {
                    return roles.stream().anyMatch(r -> r.getName().equals(role));
                }

                @Override
                public boolean isSecure() {
                    return requestContext.getSecurityContext().isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return SecurityContext.BASIC_AUTH;
                }
            });

            log.debug("Token authenticated userId={} username={}", userPrincipal.getUserId(), userPrincipal.getUsername());

        } catch (LoginException e) {
            log.warn("Token authentication failed: {}", e.getMessage());
            abortUnauthorized(requestContext, "Token invalide ou expiré");
        }
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
