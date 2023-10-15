package com.graphlql.controller;

import com.graphlql.model.Person;
import com.graphlql.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Controller
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;


    @QueryMapping
    Flux<Person> persons() {
        return personService.getPersonList();
    }

    @SubscriptionMapping()
    public Flux<Person> notifyPersonUpdates() {
        return Flux.fromStream(
                Stream.generate(() -> {

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    return new Person(UUID.randomUUID().toString().substring(0, 4), 10);
                }).limit(5));

    }

     @SubscriptionMapping()
    public Flux<String> notifyUpdates() {
        return Flux.fromIterable(
                List.of("Update-1", " Update-2", "Update-3", "Update-4", " Update-5")
        );

    }

    @QueryMapping
    Mono<Person> personById(@Argument String id, @ContextValue final String authorization) {
        return personService.findById(id);
    }
}
