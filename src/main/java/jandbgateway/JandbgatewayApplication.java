package jandbgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.Objects;

@SpringBootApplication
@EnableDiscoveryClient
public class JandbgatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(JandbgatewayApplication.class, args);
	}

}
