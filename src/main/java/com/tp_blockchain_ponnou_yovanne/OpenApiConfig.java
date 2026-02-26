package com.tp_blockchain_ponnou_yovanne;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI blockchainApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TP Blockchain API")
                        .description("API REST pour consulter et alimenter une blockchain de tickets d'evenement")
                        .version("1.0.0")
                        .contact(new Contact().name("TP R6 - Ponnou Yovanne")));
    }
}
