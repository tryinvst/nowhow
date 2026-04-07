package com.project.testApp.service;

import com.project.testApp.entity.hibirnate.PersonalOffice;
import com.project.testApp.repository.PersonalOfficeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class PersonalService {
    @Autowired
    PersonalOfficeRepository personalOfficeRepository;
    @Lazy @Autowired
    PersonalService self;


    @Transactional()
    public void savePerson (PersonalOffice personalOffice) {
        self.myFirstTransaction(personalOffice);
    }



    @Transactional
    public void myFirstTransaction (PersonalOffice personalOffice) {
        if (personalOfficeRepository.findById(personalOffice.getId()) != null) {
            PersonalOffice personalOffice1 = personalOfficeRepository.save(personalOffice);
            System.out.println(personalOffice1);
        }
    }
}
