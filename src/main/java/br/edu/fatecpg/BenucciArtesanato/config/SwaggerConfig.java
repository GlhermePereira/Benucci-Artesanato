package br.edu.fatecpg.BenucciArtesanato.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        Info apiInfo = new Info()
                .title("API E-commerce")
                .version("1.0.0")
                .description("API E-commerce com Swagger via ngrok");


        Components components = new Components()
                .addSecuritySchemes("bearer-key",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                );

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearer-key");

        return new OpenAPI()
                .info(apiInfo)
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}
