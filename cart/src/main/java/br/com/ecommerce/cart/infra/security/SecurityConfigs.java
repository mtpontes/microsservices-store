package br.com.ecommerce.cart.infra.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .requestMatchers("/cart-to-order/**").hasRole("CLIENT")
                .requestMatchers(HttpMethod.PATCH, "/carts").hasRole("CLIENT")
                .requestMatchers("/carts/**").hasRole("CLIENT")
                .requestMatchers("/anonymous/carts/**").permitAll()

            )
            .addFilterBefore(this.securityFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}