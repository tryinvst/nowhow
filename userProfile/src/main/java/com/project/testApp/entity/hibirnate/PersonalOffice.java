package com.project.testApp.entity.hibirnate;

import com.project.testApp.entity.AdressUsers;
import com.project.testApp.validation.ValidRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

//создание таблицы personal-office
@Entity (name = "personal_office")
public class PersonalOffice extends Users {

    @ValidRole(nullable = false,
            message = "Роль должна быть одной из: user, admin, developeradmin")
    @NotNull
    private String role;

    @NotNull
    private String tariff;

    @NotNull
    private String paymentmethod;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private AdressUsers address;

    protected PersonalOffice() {
    }

    // Конструктор для создания PersonalOffice из существующих Users и AdressUsers
    public PersonalOffice(Users user, AdressUsers address,
                          String role, String tariff, String paymentmethod) {
        // Копируем поля пользователя в суперкласс
        super(user.getUsername(), user.getAge(), user.getPassword(),
              user.getFirstname(), user.getLastname(), user.getPhonenumber(),
              user.getEmail(), user.getId());
        this.address = address;
        this.role = role;
        this.tariff = tariff;
        this.paymentmethod = paymentmethod;
    }

    // Геттеры и сеттеры
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTariff() {
        return tariff;
    }

    public void setTariff(String tariff) {
        this.tariff = tariff;
    }

    public String getPaymentmethod() {
        return paymentmethod;
    }

    public void setPaymentmethod(String paymentmethod) {
        this.paymentmethod = paymentmethod;
    }

    public AdressUsers getAddress() {
        return address;
    }

    public void setAddress(AdressUsers address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "PersonalOffice{" +
                "role='" + role + '\'' +
                ", tariff='" + tariff + '\'' +
                ", paymentmethod='" + paymentmethod + '\'' +
                ", address=" + address +
                "} " + super.toString();
    }
}