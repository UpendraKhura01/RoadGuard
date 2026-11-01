package com.roadguard.roadGuard_backend.CustomAnnotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    private static final String REGEX = "^[6-9][0-9]{9}$";


    @Override
    public boolean isValid(String number, ConstraintValidatorContext constraintValidatorContext) {
        if(number == null) return false;

        return number.matches(REGEX);

    }

}
