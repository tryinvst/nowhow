package com.project.testApp.service;

import com.project.testApp.dto.request.PersonRequestDto;
import com.project.testApp.dto.response.PersonResponseDto;
import com.project.testApp.entity.Person;
import com.project.testApp.entity.Roles;
import com.project.testApp.repository.PersonRepository;
import com.project.testApp.util.UserValidatiorUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class PersonalService {

    PersonRepository personRepository;
    UserValidatiorUtil userValidatiorUtil;

    public PersonalService(UserValidatiorUtil userValidatiorUtil) {
        this.userValidatiorUtil = userValidatiorUtil;
    }

    public PersonalService(PersonRepository repository) {
        this.personRepository = repository;
    }

    @Transactional
    public PersonResponseDto create(PersonRequestDto requestDto) {
        if (personRepository.existsByUserName ( requestDto.username () )) {
            throw new RuntimeException ( "Username is already exists" );
        }
        if (personRepository.existsByEmail ( requestDto.email () )) {
            throw new RuntimeException ( "Email is already exists" );
        }
        Person person = new Person ();
        person.setUserName(requestDto.username());
        person.setPassword(requestDto.password () );
        person.setPersonRoles ( Roles.Trial_user );
        person.setPhoneNumber ( requestDto.phonenumber () );
        person.setEmail ( requestDto.email () );
        Person savePerson = personRepository.save( person );

        PersonResponseDto personResponseDto = new PersonResponseDto(savePerson.getId(),
         savePerson.getPhoneNumber(), savePerson.getEmail(),
          savePerson.getUserName(), savePerson.getPassword());
        return personResponseDto;
    }

    public PersonResponseDto findPersonByUsername(String username) {
       Person findThatPerson = personRepository.findByUsername(username);
       PersonResponseDto personResponseDto = new PersonResponseDto (findThatPerson.getId(),
        findThatPerson.getPhoneNumber(),
        findThatPerson.getEmail(),
        findThatPerson.getUserName(),
        findThatPerson.getPassword());
       return personResponseDto;
    }

    public void putPerson (@RequestBody PersonRequestDto user) {
        Person existingUser = personRepository.findByUsername(user.username());
        Boolean isPasswordValid = userValidatiorUtil.password(user.password());
        if (existingUser == null || !isPasswordValid) {
            System.out.println("Пользователь не отредактирован");
            throw new IllegalArgumentException("Некорректные данные пользователя");
        }
        existingUser.setEmail(user.email());
        existingUser.setPassword(user.password());
        existingUser.setPhoneNumber(user.phonenumber());
        personRepository.save(existingUser);
        System.out.println("Пользователь успешно отредактирован");
    }

    public void deletePerson(Long id) {
        personRepository.deleteById(id);
    }

    public List<Person> findAllPersons(){
        return personRepository.findAll();
    }
}
