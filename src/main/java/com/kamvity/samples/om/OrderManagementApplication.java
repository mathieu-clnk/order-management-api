package com.kamvity.samples.om;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;

/**
 * Order Management application to purchase product from the marketplace.
 *
 * <p>
 * The application acts as a circuit breaker when the frontend application, CLI
 * or third-party requires to set an order to backend services.
 * The application implements fallback methods, timeout, retry, circuit breaker and rate limiter mechanisms.
 *
 * </p>
 * @author Mathieu C.
 */
@SpringBootApplication
public class OrderManagementApplication {

    protected WebClient webClient;

    public static void main(String[] args) {
        SpringApplication.run(OrderManagementApplication.class, args);

    }

    @Bean
    public WebClient webClientInitializer() {
        return WebClient.create();
    }

    @Bean
    public CircuitBreakerConfigCustomizer testCustomizer() {

        return CircuitBreakerConfigCustomizer
                .of("orderConfig", builder -> builder.slidingWindowSize(100)
                                                .waitDurationInOpenState(Duration.ofSeconds(5)));
    }

}