package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.jaas;

import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.model.TokenStore;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class TokenLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;

    private boolean authenticated = false;
    private UserPrincipal userPrincipal;
    private RolePrincipal rolePrincipal;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        NameCallback tokenCallback = new NameCallback("token");

        try {
            callbackHandler.handle(new Callback[]{tokenCallback});
        } catch (IOException | UnsupportedCallbackException e) {
            throw new LoginException("Erreur lors de la récupération du token: " + e.getMessage());
        }

        String token = tokenCallback.getName();

        if (token == null || token.isBlank()) {
            throw new LoginException("Token manquant");
        }

        Optional<TokenStore.TokenInfo> tokenInfo = TokenStore.getTokenInfo(token);

        if (tokenInfo.isEmpty()) {
            throw new LoginException("Token invalide ou expiré");
        }

        TokenStore.TokenInfo info = tokenInfo.get();
        this.userPrincipal = new UserPrincipal(info.getUsername(), info.getUserId());
        this.rolePrincipal = new RolePrincipal("ROLE_USER");
        this.authenticated = true;

        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        if (!authenticated) {
            return false;
        }
        subject.getPrincipals().add(userPrincipal);
        subject.getPrincipals().add(rolePrincipal);
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        cleanup();
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(userPrincipal);
        subject.getPrincipals().remove(rolePrincipal);
        cleanup();
        return true;
    }

    private void cleanup() {
        authenticated = false;
        userPrincipal = null;
        rolePrincipal = null;
    }
}
