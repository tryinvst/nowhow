package com.project.testApp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "adresses")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdressUsers {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String region;
    private String city;
    private String street;
    private String homenumber;
    private String appartment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    public AdressUsers(String region, String city, String street, String homenumber, String appartment) {
        this.region = region;
        this.city = city;
        this.street = street;
        this.homenumber = homenumber;
        this.appartment = appartment;
    }
}