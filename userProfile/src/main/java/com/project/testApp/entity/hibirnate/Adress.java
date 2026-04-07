package com.project.testApp.entity.hibirnate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table (name = "adresess")
public class Adress {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;
    String region;
    String city;
    String street;
    String homenumber;
    String appartment;

    public Adress(long id ,String region ,String city ,String street ,String homenumber ,String appartment) {
        this.id = id;
        this.region = region;
        this.city = city;
        this.street = street;
        this.homenumber = homenumber;
        this.appartment = appartment;
    }

    public long getId() {
        return id;
    }

    public String getRegion() {
        return region;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getHomenumber() {
        return homenumber;
    }

    public String getAppartment() {
        return appartment;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setHomenumber(String homenumber) {
        this.homenumber = homenumber;
    }

    public void setAppartment(String appartment) {
        this.appartment = appartment;
    }

    public Adress() {
    }
}
