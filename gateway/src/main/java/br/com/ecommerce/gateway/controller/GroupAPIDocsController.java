package br.com.ecommerce.gateway.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Hidden
@RestController
public class GroupAPIDocsController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GroupAPIDocsController.class);
    private static final Set<String> SERVICES = Set.of("gateway", "payment-ms");
    private final DiscoveryClient discoveryClient;

    public GroupAPIDocsController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }


    @GetMapping("/v3/api-docs/swagger-config")
    public Map<String, Object> swaggerConfig(ServerHttpRequest serverHttpRequest) throws URISyntaxException, UnknownHostException {
        URI uri = serverHttpRequest.getURI();
        String url = new URI(uri.getScheme(), uri.getAuthority(), null, null, null).toString();
        log.debug("--- Schema: {} ---", uri.getScheme());
        log.debug("--- Authority: {} ---", uri.getAuthority());
        log.debug("--- URL: {} ---", url);

        Map<String, Object> swaggerConfig = new LinkedHashMap<>();
        List<AbstractSwaggerUiConfigProperties.SwaggerUrl> swaggerUrls = new LinkedList<>();
        log.debug("Services: {}", discoveryClient.getServices());

        discoveryClient.getServices().stream()
            .filter(service -> !SERVICES.contains(service))
            .forEach(service -> swaggerUrls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl(
                    service, 
                    String.format("%s/%s/v3/api-docs", url, service), 
                    "Microservice: " + service.toUpperCase())));
        swaggerConfig.put("urls", swaggerUrls);
        log.debug("ROUTES: {}", swaggerConfig);
        return swaggerConfig;
    }
}