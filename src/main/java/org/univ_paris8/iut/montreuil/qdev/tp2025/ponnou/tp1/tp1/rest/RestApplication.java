package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.rest;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

@ApplicationPath("/api")
public class RestApplication extends ResourceConfig {

    public RestApplication() {
        packages(
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.rest",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.exception",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.security"
        );
        register(JacksonFeature.class);
        // Active la validation Bean Validation sur les parametres @Valid des resources
        property(ServerProperties.BV_FEATURE_DISABLE, false);
    }
}
