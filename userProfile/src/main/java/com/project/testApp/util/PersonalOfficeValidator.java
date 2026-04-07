package com.project.testApp.util;

import com.project.testApp.validation.ValidRole;  // добавьте этот импорт
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class PersonalOfficeValidator implements ConstraintValidator<ValidRole, String> {

    private static final Set<String> ALLOWED_ROLES = Set.of("user", "admin", "developeradmin");
    private boolean nullable;

    @Override
    public void initialize(ValidRole constraintAnnotation) {
        this.nullable = constraintAnnotation.nullable();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return nullable;
        }
        return ALLOWED_ROLES.contains(value);
    }
}