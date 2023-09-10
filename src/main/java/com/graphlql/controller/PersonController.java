package com.graphlql.controller;

import com.graphlql.model.Person;
import com.graphlql.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;


    @QueryMapping
    Flux<Person> persons() {
        return personService.getPersonList();
    }

    @QueryMapping
    Mono<Person> personById(@Argument String id) {
        return personService.findById(id);
    }
}
