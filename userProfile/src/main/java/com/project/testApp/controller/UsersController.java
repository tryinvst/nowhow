package com.project.testApp.controller;

import com.project.testApp.dto.request.PersonRequestDto;
import com.project.testApp.dto.response.PersonResponseDto;
import com.project.testApp.entity.Person;
import com.project.testApp.service.PersonalService;
import com.project.testApp.util.UserValidatiorUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name="users", description = "Операции с пользователем")
public class UsersController {

    public PersonalService personalService;
    public UserValidatiorUtil uservalidator;

    public UsersController(PersonalService repository ,UserValidatiorUtil uservalidator) {
        this.personalService = repository;
        this.uservalidator = uservalidator;
    }

    @PostMapping
    @Operation(summary = "Создать пользователя", description = "Возвращает модель зарегистрированного пользователя")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно создан")
    public ResponseEntity<PersonResponseDto> saveUser(@Valid @RequestBody PersonRequestDto personRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body( personalService.create(personRequestDto));

    }

    @GetMapping("/getall")
    @Operation(summary = "Получить список зарегестрированных пользователей", description = "Возвращает список пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен")
    public List<Person> findAllPersons(){
        return personalService.findAllPersons();
    }

    @PutMapping("/putUser")
    @Operation(summary = "Изменяет данные пользователя", description = "Изменяет данные пользователя")
    @ApiResponse(responseCode = "200", description = "Данные пользователя изменены")
    public void putUser(@RequestBody PersonRequestDto user) {
        personalService.putPerson(user);
    }

    @GetMapping("/userByUsername")
    @Operation(summary = "Получить данные пользователя по username", description = "Возвращает данные пользователя по username")
    @ApiResponse(responseCode = "200", description = "Данные пользователя получены успешно")
    public PersonResponseDto memory(@RequestParam String username)
    {
        return personalService.findPersonByUsername(username);
    }

   @DeleteMapping("/deleteUser")
   @Operation(summary = "Удаляет пользователя из базы данных", description = "Удаляет пользователя по username")
   @ApiResponse(responseCode = "200", description = "Данные пользователя успешно удалены")
    public void deleteUsers (@RequestParam long id) {personalService.deletePerson(id);
        }
}
