package com.tp.backend.main;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Configuration
@EnableWebFluxSecurity
public class ResourceServerConfig {
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
        http.authorizeExchange(authorize -> authorize
                .pathMatchers("/")
                .permitAll()

                .pathMatchers("/pruebas")
                .hasAuthority("ROLE_EMPLEADO")

                .pathMatchers("/vehiculos/**")
                .hasRole("USUARIO_VEHICULO")

                .pathMatchers("/admin/**")
                .hasRole("ADMIN")

                .anyExchange()
                .authenticated()

        ).oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverterReactive()))
        );
        return http.build();
    }

    // Adaptador para JwtAuthenticationConverter compatible con WebFlux
    private Converter<Jwt, Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverterReactive() {
        JwtAuthenticationConverter converter = authenticationConverter(realmRolesAuthoritiesConverter());
        return jwt -> Mono.justOrEmpty(converter.convert(jwt));
    }

    interface AuthoritiesConverter extends Converter<Map<String, Object>, Collection<GrantedAuthority>> {}

    @Bean
    AuthoritiesConverter realmRolesAuthoritiesConverter() {
        return claims -> {
            var realmAccess = Optional.ofNullable((Map<String, Object>) claims.get("realm_access"));
            var roles = realmAccess.flatMap(map -> Optional.ofNullable((List<String>) map.get("roles")));
            List<GrantedAuthority> list = roles.map(List::stream)
                    .orElse(Stream.empty())
                    .map(role -> "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .toList();

            System.out.println("Roles extraídos y prefijados: " + list);

            return list;
        };
    }

    @Bean
    JwtAuthenticationConverter authenticationConverter(
            Converter<Map<String, Object>, Collection<GrantedAuthority>> authoritiesConverter) {
        var authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            System.out.println("JwtAuthenticationConverter invocado con claims: " + jwt.getClaims());
            return authoritiesConverter.convert(jwt.getClaims());
        });
        return authenticationConverter;
    }

}
