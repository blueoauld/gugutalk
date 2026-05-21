package com.blueoauld.server.common.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import java.time.Year

class AgeValidator : ConstraintValidator<Age, Int> {

    private var min: Int = 0
    private var max: Int = Int.MAX_VALUE

    override fun initialize(constraintAnnotation: Age) {
        this.min = constraintAnnotation.min
        this.max = constraintAnnotation.max
    }

    override fun isValid(birthYear: Int?, context: ConstraintValidatorContext): Boolean {
        if (birthYear == null) {
            return true
        }

        val age = Year.now().value - birthYear
        return age in min..max
    }
}