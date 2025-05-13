package com.example.taskmanager.controller.User;

import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.service.EmailService;
import com.example.taskmanager.service.UserService;
import com.example.taskmanager.validation.PasswordValidatorChecker;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AccountCreationController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/sign-up")
    public String accountCreationForm(Model model){
        model.addAttribute("user",new MyUser());
        return "security/sign-up";
    }

    /**
     * Creates an account unless there are form errors
     * @param user
     * @param result
     * @param repassword
     * @return Returns user to the login page if account is created or
     * returns them to the sign-up page with error messages
     */
    @PostMapping("/sign-up")
    public String accountCreation(@Valid @ModelAttribute("user") MyUser user, BindingResult result, @RequestParam String repassword, HttpServletRequest request){
        PasswordValidatorChecker pvc = new PasswordValidatorChecker();
        PasswordValidator validatorChecker = pvc.getValidation();

        if(userService.getUserByEmail(user.getEmail()) != null){
            result.rejectValue("email", "error.user", "Account with email already exists!");
        }

        if(userService.getUserByUsername(user.getUsername()) != null){
            result.rejectValue("username", "error.user", "Account with this username already exists!");
        }

        if(!user.getPassword().equals(repassword)){
            result.rejectValue("password", "error.user", "Passwords don't match");
        }
        RuleResult ruleResult = validatorChecker.validate(new PasswordData(user.getPassword()));
        if(!ruleResult.isValid()){
            List<String> errors = validatorChecker.getMessages(ruleResult);
            for (String error: errors) {
                result.rejectValue("password", "error.user", error);
            }
        }

        if(result.hasErrors()){
            return "security/sign-up";
        }
        userService.createUserAccount(user);
        emailService.sendMail(user.getEmail(),"Welcome to Task Manager Web", emailService.SignUpEmail(user,request));

        return "security/login";
    }

}
