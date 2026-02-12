package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

@ApplicationPath("/api")
public class RestApplication extends ResourceConfig {

    public RestApplication() {
        packages(
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.controller",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.controller",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.filter",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception"
        );
        register(JacksonFeature.class);
        property(ServerProperties.BV_FEATURE_DISABLE, false);
    }
}
