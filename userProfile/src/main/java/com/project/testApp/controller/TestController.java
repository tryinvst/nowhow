package com.project.testApp.controller;/*package com.project.testApp.controller;

import com.project.testApp.entity.AdressUsers;
import com.project.testApp.entity.Person;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

//контроллер принимает запросы из postman
@RestController
public class TestController {

    List <Person> names = new ArrayList<> ();

    @PostMapping ("/post")
    public void saveUserToList (@RequestBody Person param) {
        names.add(param);
    }

    @GetMapping ("/get")
    public Person getUser (@RequestParam int param) {
        return names.get (param);
    }

}
*/