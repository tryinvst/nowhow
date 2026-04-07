package com.project.testApp.controller;

import com.project.testApp.entity.hibirnate.Users;
import com.project.testApp.repository.UserRepository;
import com.project.testApp.util.UserValidatiorUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//принимает запросы от Postman для работы с таблицей users
@RestController
@RequestMapping
public class UsersController {

    public UserRepository repository;
    public UserValidatiorUtil uservalidator;

    public UsersController(UserRepository repository ,UserValidatiorUtil uservalidator) {
        this.repository = repository;
        this.uservalidator = uservalidator;
    }

    @PostMapping
    public void saveUser(@Valid @RequestBody Users users) {
        if (repository.findByUsername ( users.getUsername () ) != null)
            throw new RuntimeException ( "Такой пользователь уже существует!" );
        else if (!uservalidator.password (users.getPassword()))
        {
          throw new RuntimeException ("Пароль не валиден, отсутствует заглавная буква или спец символы");
        }
        else {
            repository.save (users);
        }
    }

    @PutMapping
    public void putUser(@RequestBody Users user) {
        Users existingUser = repository.findByUsername(user.getUsername());
        boolean isPasswordValid = uservalidator.password(user.getPassword());
        if (existingUser == null || !isPasswordValid) {
            System.out.println("Пользователь не отредактирован");
            throw new IllegalArgumentException("Некорректные данные пользователя");
        }
        repository.save(user);
        System.out.println("Пользователь успешно отредактирован");
    }

    @GetMapping
    public Users memory(@RequestParam String username)
    {
        return repository.findByUsername(username);
    }

   @DeleteMapping
    public void deleteUsers (@RequestParam long id) {
      repository.deleteById (id);
        }
}
