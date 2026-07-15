package br.com.ecommerce.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class AddHeaderGlobalFilterImpl implements GlobalFilter {

    @Value("${api.security.gateway.name}")
    private String gatewayName;

    private final WebClient.Builder webClientBuilder;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AddHeaderGlobalFilterImpl(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate();
        requestBuilder.headers(headers -> headers.keySet().removeIf(key -> key.toLowerCase().startsWith("x-auth-user-")));
        requestBuilder.header("X-Forwarded-By", this.gatewayName);
        
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().name();
        boolean isPublic = this.isPublicRoute(path, method);
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (isPublic) {
                if (authHeader != null) requestBuilder.headers(h -> h.remove("Authorization"));
                ServerWebExchange mutatedExchange = exchange.mutate().request(requestBuilder.build()).build();
                return chain.filter(mutatedExchange);
            }
            return this.onError(exchange, "Token de autenticação ausente ou inválido", HttpStatus.UNAUTHORIZED);
        }
        
        String token = authHeader.substring(7);

        return this.webClientBuilder.build()
            .get()
            .uri("http://auth-ms/auth")
            .header("Authorization", "Bearer " + token)
            .retrieve()
            .bodyToMono(UserDTO.class)
            .flatMap(userDto -> {
                requestBuilder.header("X-auth-user-id", userDto.id());
                requestBuilder.header("X-auth-user-username", userDto.username());
                requestBuilder.header("X-auth-user-role", userDto.role());
                
                ServerWebExchange mutatedExchange = exchange.mutate().request(requestBuilder.build()).build();
                return chain.filter(mutatedExchange);
            })
            .onErrorResume(error -> {
                if (isPublic) {
                    requestBuilder.headers(h -> h.remove("Authorization"));
                    ServerWebExchange mutatedExchange = exchange.mutate().request(requestBuilder.build()).build();
                    return chain.filter(mutatedExchange);
                }
                return this.onError(exchange, "Token de autenticação ausente ou inválido", HttpStatus.UNAUTHORIZED);
            });
    }

    private boolean isPublicRoute(String path, String method) {
        if (this.pathMatcher.match("/products-ms/products/**", path) && "GET".equalsIgnoreCase(method)) return true;
        if (this.pathMatcher.match("/products-ms/departments/**", path) && "GET".equalsIgnoreCase(method)) return true;
        if (this.pathMatcher.match("/products-ms/manufacturers/**", path) && "GET".equalsIgnoreCase(method)) return true;
        if (this.pathMatcher.match("/accounts-ms/auth", path) && "POST".equalsIgnoreCase(method)) return true;
        if (this.pathMatcher.match("/v3/api-docs/**", path) || 
            this.pathMatcher.match("/**/v3/api-docs/**", path) ||
            this.pathMatcher.match("/swagger-ui/**", path) || 
            this.pathMatcher.match("/webjars/**", path)) return true;
            
        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        String body = String.format(
            "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
            timestamp,
            status.value(),
            status.getReasonPhrase(),
            message
        );
        
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }


    private record UserDTO(String id, String username, String role) {}
}