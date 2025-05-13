package com.example.taskmanager.controller.User;

import com.example.taskmanager.repo.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
public class AuthenticationController {

    @RequestMapping("/login")
    public String login(@RequestParam(name = "error", defaultValue = "false") String errors, Model model) {
        model.addAttribute("error", Boolean.parseBoolean(errors));
        return "security/login";
    }

    @RequestMapping("/login-success")
    public String success() {
        return "redirect:/home";
    }

}
