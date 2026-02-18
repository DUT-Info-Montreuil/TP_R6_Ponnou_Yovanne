package org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.config;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import javax.ws.rs.ApplicationPath;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.jaas.JaasConfig;

import java.util.Set;

@Slf4j
@ApplicationPath("/api")
public class RestApplication extends ResourceConfig {

    public RestApplication() {
        JaasConfig.install();
        log.info("JAAS configuration installed (MasterAnnonceLogin, MasterAnnonceToken)");

        packages(
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.controller",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.controller",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.filter",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.controller",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.logging",
                "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.exception"
        );
        register(JacksonFeature.class);
        registerOpenApi();
        property(ServerProperties.BV_FEATURE_DISABLE, false);
    }

    private void registerOpenApi() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("MasterAnnonce API")
                        .version("1.0.0")
                        .description("API REST stateless pour la gestion des annonces, utilisateurs et categories."))
                .components(new Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("UUID")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));

        SwaggerConfiguration swaggerConfiguration = new SwaggerConfiguration()
                .openAPI(openAPI)
                .prettyPrint(true)
                .resourcePackages(Set.of(
                        "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.annonce.controller",
                        "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.user.controller",
                        "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.category.controller",
                        "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.auth.controller",
                        "org.univ_paris8.iut.montreuil.qdev.tp2025.ponnou.tp1.tp1.common.controller"
                ));

        OpenApiResource openApiResource = new OpenApiResource();
        openApiResource.setOpenApiConfiguration(swaggerConfiguration);
        register(openApiResource);
    }
}
