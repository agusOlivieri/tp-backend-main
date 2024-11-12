package com.tp.backend.main;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GWConfig {

    @Bean
    public RouteLocator configurarRutas(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p
                        .path("/pruebas").uri("http://localhost:8082")
                )
                .route(r -> r
                        .path("/pruebas/finalizar")
                        .uri("http://localhost:8082")
                )
                .route(p -> p
                        .path("/vehiculos/posicion")
                        .uri("http://localhost:8083")
                )
                .route(p -> p
                        .path("/reportes/incidentes")
                        .uri("http://localhost:8082")
                )
                .route(p -> p
                        .path("/reportes/incidentes/empleado")
                        .uri("http://localhost:8082")
                )
                .route(p -> p
                        .path("/reportes/pruebas/vehiculo")
                        .uri("http://localhost:8082")
                )

                .build();
    }
}

