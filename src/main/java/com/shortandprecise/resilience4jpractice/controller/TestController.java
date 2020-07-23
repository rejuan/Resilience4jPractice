package com.shortandprecise.resilience4jpractice.controller;

import com.shortandprecise.resilience4jpractice.service.WebClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {

	private WebClientService webClientService;

	@Autowired
	public TestController(WebClientService webClientService) {
		this.webClientService = webClientService;
	}

	@GetMapping("/mock")
	public Mono<String> mock() {
		return webClientService.getResponse().onErrorResume(throwable ->
				Mono.just(throwable.getMessage()));
	}
}
