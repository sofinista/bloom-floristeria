package com.floristeria.bloom.configuraciones;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bloomOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API Floristería Bloom")
                .version("2.0")
                .description("Gestión de clientes, pedidos, flores y pagos de la Floristería Bloom"));
    }
}