package com.project.testApp.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long id;
    private String userName; //дедубликация на username
    private String password; //не менее 8 символов, 1 заглавная буква, 1 спец. символ
    private String phoneNumber; //в международном формате, до 14 символов
    private String email; // обязательно содержит собачку
    private Roles personRoles;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Roles getPersonRoles() {
        return personRoles;
    }

    public void setPersonRoles(Roles personRoles) {
        this.personRoles = personRoles;
    }

    public List<AdressUsers> getRegion() {
        return region;
    }

    public void setRegion(List<AdressUsers> region) {
        this.region = region;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    List<AdressUsers>region = new ArrayList<> ();

}
