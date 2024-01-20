package ru.berdnikov.telegrambot.services.personService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.berdnikov.telegrambot.dto.PersonDTO;
import ru.berdnikov.telegrambot.entity.Person;
import ru.berdnikov.telegrambot.repositories.PeopleRepository;

@Service
@Transactional
public class PersonRegistrationService {

    private final PeopleRepository peopleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PersonRegistrationService(PeopleRepository peopleRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.peopleRepository = peopleRepository;
    }

    public String register(PersonDTO personDTO){
        Person person = convertToPerson(personDTO);
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        person.setRole("ROLE_USER");

        if (peopleRepository.findByUsername(person.getUsername()).isEmpty()) {
            peopleRepository.save(person);
            return "The person has been added successfully";
        }
        return "A user with this username already exists";
    }

    private Person convertToPerson(PersonDTO personDTO){
        return this.modelMapper.map(personDTO, Person.class);
    }
}
