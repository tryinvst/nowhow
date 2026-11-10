package com.project.testApp.entity;

import com.project.common.Category;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    /**
     * Дедубликация на username.
     */
    private String userName;
    /**
     * Хранится только BCrypt-хэш, наружу не отдаётся.
     * <p>Не менее 8 символов, 1 заглавная буква, 1 спец. символ.
     */
    @JsonIgnore
    private String password;
    /**
     * В международном формате, до 14 символов.
     */
    private String phoneNumber;
    /**
     * Обязательно содержит собачку.
     */
    private String email;
    /**
     * Строкой, чтобы новые роли не сдвигали номера.
     */
    @Enumerated(EnumType.STRING)
    private Roles personRoles;

    /**
     * Публичный профиль мастера (у учеников не показывается)
     */
    private String displayName;
    @Enumerated(EnumType.STRING)
    private Category specialization;
    @Column(length = 2000)
    private String about;
    private Integer experienceYears;
    private String avatarStorageFilename;

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
