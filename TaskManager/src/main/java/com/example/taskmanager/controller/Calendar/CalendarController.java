package com.example.taskmanager.controller.Calendar;

import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/calendar")
public class CalendarController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public String calendarPage(Model model, Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user",user);
        return "calendar/calendar";
    }
}
