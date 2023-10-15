package com.graphlql;

import java.net.URI;

import com.graphlql.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.WebSocketGraphQlTester;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;

/**
 * GraphQL over WebSocket integration tests.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebFluxWebSocketSampleIntegrationTests {

    @Value("http://localhost:8080/graphql")
    private String baseUrl;

    private GraphQlTester graphQlTester;


    @BeforeEach
    void setUp() {
        URI url = URI.create(baseUrl);
        this.graphQlTester = WebSocketGraphQlTester.builder(baseUrl, new ReactorNettyWebSocketClient()).build();
    }

    @Test
    void subscriptionWithEntityPath() {
        Flux<Person> result = this.graphQlTester.document("subscription { notifyPersonUpdates { id age } }")
                .executeSubscription()
                .toFlux("notifyPersonUpdates", Person.class);

        StepVerifier.create(result)
                .expectNextMatches(person -> person.getAge() == 10)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    void subscription() {
        Flux<String> result = this.graphQlTester.document("subscription { notifyUpdates } ")
                .executeSubscription()
                .toFlux("notifyUpdates", String.class);

        StepVerifier.create(result)
                .expectNext("Update-1")
                .expectNextCount(4)
                .verifyComplete();
    }
}