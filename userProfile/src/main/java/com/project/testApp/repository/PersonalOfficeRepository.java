package com.project.testApp.repository;

import com.project.testApp.entity.hibirnate.PersonalOffice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalOfficeRepository extends JpaRepository <PersonalOffice, Long> {
    PersonalOffice findByRole (String role);
    PersonalOffice findByTariff(String tariff);
    PersonalOffice findByPaymentmethod (String paymentmethod);

}
