package br.com.ecommerce.orders.infra.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.ecommerce.common.filter.SecurityFilter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfigs {

    @Value("${api.security.gateway.name}") 
    private String gatewayName;


    @Bean
    public OncePerRequestFilter securityFilter() {
        return new SecurityFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/v3/api-docs/**", "/swagger-ui.html/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/admin/orders/**").hasRole("ADMIN")
                .requestMatchers("/client/orders/**").hasRole("CLIENT")
                .requestMatchers("/internal/**").access((authentication, requestContext) -> {
                    log.debug("GATEWAY NAME: {}", gatewayName);
                    boolean isGatewayOrigin = Optional.ofNullable(requestContext.getRequest().getHeader("X-Forwarded-By"))
                        .map(header -> header.equalsIgnoreCase(gatewayName))
                        .orElse(false);
                    
                    log.debug("IS GATEWAY ORIGIN: {}", isGatewayOrigin);
                    return new AuthorizationDecision(!isGatewayOrigin);
                })
            )
            .addFilterBefore(this.securityFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}