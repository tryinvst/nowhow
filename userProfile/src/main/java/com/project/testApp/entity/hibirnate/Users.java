package com.project.testApp.entity.hibirnate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

//создание таблицы Users
@Entity(name = "users")
public class Users {

    @NotNull
    String username;
    @NotNull
    Integer age;
    @NotNull
    @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
    String password;
    @NotNull
    String firstname;
    @NotNull
    String lastname;
    @NotNull
    String phonenumber;
    @NotNull
    @Email
    String email;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    public Users(String username ,Integer age ,String password ,String firstname ,
                 String lastname ,String phonenumber ,String email ,
                 long id) {
        this.username = username;
        this.age = age;
        this.password = password;
        this.firstname = firstname;
        this.lastname = lastname;
        this.phonenumber = phonenumber;
        this.id = id;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Users() {
    }

    public String getPassword() {
        return password;
    }

    public long getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public Integer getAge() {
        return age;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }
}


