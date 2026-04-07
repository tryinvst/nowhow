package com.project.testApp.repository;

import com.project.testApp.entity.hibirnate.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Users findByUsername (String username);
    Users findByAge (Integer age);
    Users findByPassword (String password);
    Users findByfirstname (String firstname);
    Users findBylastname (String lastname);
    Users findByphonenumber (String phonenumber);

}
