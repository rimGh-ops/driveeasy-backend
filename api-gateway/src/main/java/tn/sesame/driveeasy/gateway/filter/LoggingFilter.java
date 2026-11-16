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
import java.util.UUID;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();

        // Génération d'un identifiant unique pour chaque requête
        String requestId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Injection de l'en-tête X-Request-Id pour le traçage
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Request-Id", requestId)
                .build();

        log.info("┌─[GATEWAY IN ] [{} ] ► {} {} | Client: {}",
                requestId,
                request.getMethod(),
                request.getURI().getPath(),
                request.getRemoteAddress());

        return chain.filter(exchange.mutate().request(request).build())
                .then(Mono.fromRunnable(() -> {
                    long duree = System.currentTimeMillis() - start;
                    ServerHttpResponse response = exchange.getResponse();

                    // Ajout des en-têtes de métriques dans la réponse
                    response.getHeaders().add("X-Request-Id", requestId);
                    response.getHeaders().add("X-Gateway-Latency-Ms", String.valueOf(duree));
                    response.getHeaders().add("X-Gateway-Timestamp", Instant.now().toString());

                    log.info("└─[GATEWAY OUT] [{}] ◄ statut={} | durée={} ms | path={}",
                            requestId,
                            response.getStatusCode(),
                            duree,
                            request.getURI().getPath());
                }));
    }

    @Override
    public int getOrder() {
        return -1; // Plus haute priorité : intercepte tout en premier
    }
}
