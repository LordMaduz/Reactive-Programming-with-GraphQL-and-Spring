package com.graphlql.service;


import com.graphlql.exception.ValidationException;
import com.graphlql.model.Person;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    List<Person> personList = new ArrayList<>(List.of(new Person("1", 30), new Person("2", 20)));

    public Flux<Person> getPersonList() {
        return Flux.fromIterable(personList);
    }

    public Mono<Person> findById(final String id) {
        return Mono.fromCallable(() -> {
            Optional<Person> optional = personList.stream().filter(p -> p.getId().equals(id)).findAny();
            if (optional.isPresent()) {
                return optional.get();
            } else {
                throw new ValidationException("No Person With Given Id ");
            }

        });
    }
}
