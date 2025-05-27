package com.example.eshop.security.validation;

import com.example.eshop.model.dto.common.PasswordFields; // Import the new base DTO
import com.example.eshop.util.PasswordMatches;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// Validator now works for PasswordFields
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, PasswordFields> {

    @Override
    public boolean isValid(PasswordFields dto, ConstraintValidatorContext context) {
        // Handle nulls: Let @NotBlank on individual fields handle if they are null/empty.
        // This validator only checks the *match* if both are provided.
        if (dto.password() == null || dto.confirmPassword() == null) {
            return true;
        }

        boolean isValid = dto.password().equals(dto.confirmPassword());
        if (!isValid) {
            context.disableDefaultConstraintViolation();

            // Add custom violation to the 'confirmPassword' field
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }
        return isValid;
    }
}
