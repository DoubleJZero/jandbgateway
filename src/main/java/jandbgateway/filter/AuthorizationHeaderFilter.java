package jandbgateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public AuthorizationHeaderFilter(JwtTokenProvider jwtTokenProvider) {
        super(Config.class);
        this.jwtTokenProvider = jwtTokenProvider;
    }

    static class Config {

    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().toString();

            if("/user-service/login".equals(path)) return chain.filter(exchange);

            HttpHeaders headers = request.getHeaders();
            if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "No authorization header");
            }

            String authorizationHeader = Objects.requireNonNull(headers.get(HttpHeaders.AUTHORIZATION)).get(0);

            // JWT 토큰 판별
            String token = authorizationHeader.replace("Bearer", "");

            jwtTokenProvider.validateJwtToken(token);

            String subject = jwtTokenProvider.getUserId(token);

            if (subject.equals("feign")) return chain.filter(exchange);

            if (!jwtTokenProvider.getRoles(token).contains("USER")) {
                return onError(exchange, "권한 없음");
            }

            ServerHttpRequest newRequest = request.mutate()
                    .header("user-id", subject)
                    .build();

            return chain.filter(exchange.mutate().request(newRequest).build());
        };
    }

    // Mono(단일 값), Flux(다중 값) -> Spring WebFlux
    private Mono<Void> onError(ServerWebExchange exchange, String errorMsg) {
        log.error(errorMsg);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        return response.setComplete();
    }
}
