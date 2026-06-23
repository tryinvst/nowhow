package com.project.testApp.repository;

import com.project.testApp.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {
    Boolean existsByUserName(String username);
    Boolean existsByEmail(String email);
    Person findByUsername(String username);

}
