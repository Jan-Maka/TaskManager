package com.example.taskmanager.ServiceTests;

import com.example.taskmanager.validation.PasswordValidatorChecker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.test.util.AssertionErrors.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest
public class PasswordValidationTest {
    @DisplayName("Test password strength")
    @Test
    public void TestPasswordStrengthValidator(){
        PasswordValidatorChecker psv = new PasswordValidatorChecker();
        PasswordValidator validator = psv.getValidation();

        RuleResult result = validator.validate(new PasswordData("password"));
        assertFalse("Password test failed with weak password", result.isValid() );

        result = validator.validate(new PasswordData("Password123!"));
        assertTrue("Password test passed with strong password", result.isValid() );
    }
}
