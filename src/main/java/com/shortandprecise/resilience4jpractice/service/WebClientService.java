package com.shortandprecise.resilience4jpractice.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.netty.channel.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class WebClientService {

	public static final Logger LOGGER = LoggerFactory.getLogger(WebClientService.class);

	CircuitBreakerRegistry circuitBreakerRegistry;
	WebClient webClient;

	@Autowired
	public WebClientService(WebClient webClient,
							CircuitBreakerRegistry circuitBreakerRegistry) {
		this.webClient = webClient;
		this.circuitBreakerRegistry = circuitBreakerRegistry;
	}

	public Mono<String> getResponse() {
		CircuitBreaker circuitBreaker = circuitBreakerRegistry.find("webclient").get();

		return webClient.get()
				.uri("http://localhost:8089/mock")
				.exchange()
				.timeout(Duration.ofMillis(1000))
				.flatMap(clientResponse -> {
					if (clientResponse.statusCode().isError()) {
						throw new RuntimeException("Error");
					}
					return clientResponse.bodyToMono(String.class);
				})
				.onErrorResume(throwable -> {
					if (throwable instanceof TimeoutException ||
							throwable instanceof io.netty.handler.timeout.TimeoutException ||
							throwable instanceof ConnectTimeoutException) {
						LOGGER.error("timeout happened");
					}
					return Mono.error(throwable);
				})
				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
	}
}
