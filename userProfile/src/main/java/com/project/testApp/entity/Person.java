package com.project.testApp.entity;

import java.util.ArrayList;
import java.util.List;

public class Person {

    public Person() {
    }

    public List<AdressUsers> getRegion() {
        return region;
    }

    public void setRegion(List<AdressUsers> region) {
        this.region = region;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    String userName; //дедубликация на username
    String password; //не менее 8 символов, 1 заглавная буква, 1 спец. символ
    String phoneNumber; //в международном формате, до 14 символов
    String email; // обязательно содержит собачку

    List<AdressUsers>region = new ArrayList<> ();

    public Person (String userName, String password) {
       this.userName = userName;
       this.password = password;

    }

    public Person(String userName ,String password ,String phoneNumber ,String email ,List<AdressUsers> region) {
        this.userName = userName;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.region = region;
    }

    @Override
    public String toString() {
        return "Person{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
