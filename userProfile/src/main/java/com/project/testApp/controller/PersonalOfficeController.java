package com.project.testApp.controller;

import com.project.testApp.entity.hibirnate.PersonalOffice;
import com.project.testApp.repository.PersonalOfficeRepository;
import com.project.testApp.repository.UserRepository;
import com.project.testApp.service.PersonalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/personal-offices")
public class PersonalOfficeController {

    private final UserRepository userRepository;
    private final PersonalOfficeRepository personalOfficeRepository;
    public final PersonalService personalService;

    // Единый конструктор для внедрения обоих репозиториев
    public PersonalOfficeController(UserRepository userRepository,
                                    PersonalOfficeRepository personalOfficeRepository,
                                    PersonalService personalService) {
        this.userRepository = userRepository;
        this.personalOfficeRepository = personalOfficeRepository;
        this.personalService = personalService;
    }

    @PostMapping
    public ResponseEntity<PersonalOffice> uploadUsers(@Valid @RequestBody PersonalOffice personalOffice) {
        // Сохраняем PersonalOffice через соответствующий репозиторий
       personalService.savePerson(personalOffice);
        return ResponseEntity.status(HttpStatus.CREATED).body(personalOffice);
    }

    @GetMapping
    public ResponseEntity<PersonalOffice> getByRole(@RequestParam String role) {
        // Поиск по роли с использованием метода репозитория
        PersonalOffice office = personalOfficeRepository.findByRole(role);
        if (office == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(office);
    }
}