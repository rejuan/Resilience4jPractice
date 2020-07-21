package com.shortandprecise.resilience4jpractice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ApplicationStartUp implements ApplicationRunner {

	CircuitBreakerRegistry circuitBreakerRegistry;

	public ApplicationStartUp(CircuitBreakerRegistry circuitBreakerRegistry) {
		this.circuitBreakerRegistry = circuitBreakerRegistry;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
				.failureRateThreshold(60)
				.slowCallRateThreshold(100)
				.slowCallDurationThreshold(Duration.ofMillis(60000))
				.permittedNumberOfCallsInHalfOpenState(1)
				.slidingWindowType(
						io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
				.slidingWindowSize(10)
				.minimumNumberOfCalls(10)
				.waitDurationInOpenState(Duration.ofMillis(10000))
				.automaticTransitionFromOpenToHalfOpenEnabled(false)
				.recordException(throwable -> true)
				.build();

		circuitBreakerRegistry.circuitBreaker("webclient", circuitBreakerConfig);
	}
}
