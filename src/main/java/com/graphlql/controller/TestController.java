package com.graphlql.controller;

import com.graphlql.security.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final JWTUtil jwtUtil;

    @GetMapping("/register")
    public Mono<String> helllo()
    {
        return Mono.just(jwtUtil.generateToken());
    }
}
