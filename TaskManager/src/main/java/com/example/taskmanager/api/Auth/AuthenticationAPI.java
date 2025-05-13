package com.example.taskmanager.api.Auth;

import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.repo.User.UserRepository;
import com.example.taskmanager.service.PasswordTokenService;
import com.example.taskmanager.service.UserService;
import com.example.taskmanager.validation.PasswordValidatorChecker;
import jakarta.servlet.http.HttpServletRequest;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationAPI {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordTokenService passwordTokenService;

    @Autowired
    private UserService userService;

    /**
     * Checks whether the email input exists in the database
     * @param email
     * @return true if one does exist; false if it doesn't
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<?> checkEmailExists(@PathVariable("email") String email){
        if(userRepo.existsByEmail(email)){
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.NOT_FOUND);
    }

    /**
     * Checks if a password is valid
     * @param password
     * @return true if the password is valid; false if the password is not valid
     */
    @GetMapping("/password-valid/{password}")
    public ResponseEntity<?> isPasswordValid(@PathVariable("password") String password){
        PasswordValidatorChecker pvc = new PasswordValidatorChecker();
        PasswordValidator validatorChecker = pvc.getValidation();
        RuleResult ruleResult = validatorChecker.validate(new PasswordData(password));
        if(ruleResult.isValid()){
            return new ResponseEntity<>(true,HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.NOT_ACCEPTABLE);
    }

    /**
     * Sends the password reset email to the user if email exists
     * @param request
     * @param email
     * @return 200 status code that the email has been sent
     */
    @PostMapping("/reset/password/{email}")
    public HttpStatus sendResetEmail(HttpServletRequest request, @PathVariable("email") String email){
        if(userRepo.existsByEmail(email)){
            userService.sendResetPasswordEmail(request, userRepo.findByEmail(email));
            return HttpStatus.OK;
        }
        return HttpStatus.BAD_REQUEST;
    }

    /**
     * Updates the users password
     * @param token
     * @param password
     * @return 200 status code that the password has been successfully changed
     */
    @PatchMapping("/reset/password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String password){
        MyUser user = passwordTokenService.getUserByToken(token);
        userService.setNewPasswordForUser(user, password);
        passwordTokenService.deleteTokenAfterUse(user.getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Checks if the user password typed in is the current logged-in users password
     * @param password
     * @param principal
     * @return
     */
    @GetMapping("/is-user-password/{password}")
    public ResponseEntity<?> isUsersPassword(@PathVariable String password,Principal principal){
        return new ResponseEntity<>(userService.isUsersPassword(principal,password),HttpStatus.OK);
    }
}
