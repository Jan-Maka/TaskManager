package com.example.taskmanager.validation;

import org.passay.*;
import java.util.Arrays;
public class PasswordValidatorChecker {

    public PasswordValidator getValidation(){
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 30),
                new CharacterRule(EnglishCharacterData.UpperCase,1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule()));
        return validator;
    }
}
