package com.project.testApp.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "addresses")
public class AdressUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;   // обязательное поле-идентификатор

    private String region;
    private String city;
    private String street;
    private String homenumber;
    private String appartment;

    public AdressUsers() {
    }

    public AdressUsers(String region, String city, String street, String homenumber, String appartment) {
        this.region = region;
        this.city = city;
        this.street = street;
        this.homenumber = homenumber;
        this.appartment = appartment;
    }

    // Геттеры и сеттеры для всех полей (включая id)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHomenumber() {
        return homenumber;
    }

    public void setHomenumber(String homenumber) {
        this.homenumber = homenumber;
    }

    public String getAppartment() {
        return appartment;
    }

    public void setAppartment(String appartment) {
        this.appartment = appartment;
    }
}