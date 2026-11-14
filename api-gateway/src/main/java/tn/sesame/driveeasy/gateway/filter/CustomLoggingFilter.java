package tn.sesame.driveeasy.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class CustomLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CustomLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        log.info("[API GATEWAY IN] ==> Method: {} | Path: {} | ClientIP: {}",
                request.getMethod(), request.getURI().getPath(), request.getRemoteAddress());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            ServerHttpResponse response = exchange.getResponse();

            response.getHeaders().add("X-Gateway-Latency-Ms", String.valueOf(duration));
            response.getHeaders().add("X-Gateway-Timestamp", Instant.now().toString());

            log.info("[API GATEWAY OUT] <== Status: {} | Path: {} | Duration: {}ms",
                    response.getStatusCode(), request.getURI().getPath(), duration);
        }));
    }

    @Override
    public int getOrder() {
        return -1; // Priorité élevée pour intercepter dès l'entrée
    }
}
