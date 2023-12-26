package jandbgateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

/**
 * AuthorizationHeaderFilter
 *
 * <pre>
 * 코드 히스토리 (필요시 변경사항 기록)
 * </pre>
 *
 * @author JandB
 * @since 1.0
 */
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
                try {
                    return onError(exchange, "No authorization header");
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            String authorizationHeader = Objects.requireNonNull(headers.get(HttpHeaders.AUTHORIZATION)).get(0);

            // JWT 토큰 판별
            String token = authorizationHeader.replace("Bearer", "").trim();

            jwtTokenProvider.validateJwtToken(token);

            String subject = jwtTokenProvider.getUserId(token);

            if (subject.equals("feign")) return chain.filter(exchange);

            if (!jwtTokenProvider.getRoles(token).contains("USER")) {
                try {
                    return onError(exchange, "권한 없음");
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            ServerHttpRequest newRequest = request.mutate()
                    .header("user-id", subject)
                    .build();

            return chain.filter(exchange.mutate().request(newRequest).build());
        };
    }

    // Mono(단일 값), Flux(다중 값) -> Spring WebFlux
    private Mono<Void> onError(ServerWebExchange exchange, String errorMsg) throws JsonProcessingException {
        log.error("######################################################");
        log.error(errorMsg);
        log.error("######################################################");

        return exchange.getResponse().writeWith(Mono.just(new DefaultDataBufferFactory().wrap(new ObjectMapper().writeValueAsBytes(Map.of("status",String.valueOf(HttpStatus.UNAUTHORIZED.value()), "message", HttpStatus.UNAUTHORIZED.toString(),"code",HttpStatus.UNAUTHORIZED.name())))));
    }
}
