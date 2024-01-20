package ru.berdnikov.telegrambot.services.personService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berdnikov.telegrambot.entity.Person;
import ru.berdnikov.telegrambot.repositories.PeopleRepository;

import java.util.Optional;

@Service
@Transactional
public class PersonService {

    private final PeopleRepository peopleRepository;

    @Autowired
    public PersonService(PeopleRepository peopleRepository) {
        this.peopleRepository = peopleRepository;
    }

    @Transactional(readOnly = true)
    public Person getByUsername(String username){
        Optional<Person> person = peopleRepository.findByUsername(username);
        return person.orElse(null);
    }
}
