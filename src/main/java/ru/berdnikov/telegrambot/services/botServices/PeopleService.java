package ru.berdnikov.telegrambot.services.botServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.berdnikov.telegrambot.entity.Person;
import ru.berdnikov.telegrambot.repositories.PeopleRepository;

import java.util.Optional;

@Service
public class PeopleService {

    private final PeopleRepository peopleRepository;

    @Autowired
    public PeopleService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    public Person getByUsername(String username){
        Optional<Person> person = peopleRepository.findByUsername(username);
        return person.orElse(null);
    }
}
