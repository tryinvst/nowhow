package com.project.testApp.util;


import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
//проверка пароля на валидацию
@Service
public class UserValidatiorUtil {
 public boolean password (String password) {
     String pattern = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
     Pattern p = Pattern.compile(pattern);
     Matcher m = p.matcher((password));
     return m.matches();
 }
}
