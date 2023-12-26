package jandbgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * JandbgatewayApplication
 *
 * <pre>
 * 코드 히스토리 (필요시 변경사항 기록)
 * </pre>
 *
 * @author JandB
 * @since 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class JandbgatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(JandbgatewayApplication.class, args);
	}

}
