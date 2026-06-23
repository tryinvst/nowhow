package com.project.testApp.controller;

import com.project.testApp.dto.request.PersonRequestDto;
import com.project.testApp.dto.response.PersonResponseDto;
import com.project.testApp.entity.Person;
import com.project.testApp.service.PersonalService;
import com.project.testApp.util.UserValidatiorUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//принимает запросы от Postman для работы с таблицей users
@RestController
@RequestMapping
public class UsersController {

    public PersonalService personalService;
    public UserValidatiorUtil uservalidator;

    public UsersController(PersonalService repository ,UserValidatiorUtil uservalidator) {
        this.personalService = repository;
        this.uservalidator = uservalidator;
    }

    @PostMapping
    public ResponseEntity<PersonResponseDto> saveUser(@Valid @RequestBody PersonRequestDto personRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body( personalService.create(personRequestDto));

    }

    @GetMapping("/getall")
    public List<Person> findAllPersons(){
        return personalService.findAllPersons();
    }

    @PutMapping("/putUser")
    public void putUser(@RequestBody PersonRequestDto user) {
        personalService.putPerson(user);
    }

    @GetMapping("/userByUsername")
    public PersonResponseDto memory(@RequestParam String username)
    {
        return personalService.findPersonByUsername(username);
    }

   @DeleteMapping("/deleteUser")
    public void deleteUsers (@RequestParam long id) {personalService.deletePerson(id);
        }
}
