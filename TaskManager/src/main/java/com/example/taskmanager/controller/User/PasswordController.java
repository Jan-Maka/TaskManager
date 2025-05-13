package com.example.taskmanager.controller.User;

import com.example.taskmanager.repo.User.UserRepository;
import com.example.taskmanager.service.PasswordTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PasswordController {

    @Autowired
    private PasswordTokenService passwordTokenService;

    @RequestMapping("/reset-password")
    public String resetPasswordForm(){return "security/reset-password";}

    /**
     * Used to check a password reset token if the token is valid then go to passsword reset page
     * @param token
     * @param model
     * @return
     */
    @GetMapping("/reset-password/check-token/{token}")
    public String checkTokenValid(@PathVariable("token")String token, Model model){
        if(!passwordTokenService.resetTokenExistsByToken(token) || !passwordTokenService.isTokenExpired(token)){
            model.addAttribute("token", token);
            return "security/password-reset";
        }else{
            return "security/login";
        }
    }


}
