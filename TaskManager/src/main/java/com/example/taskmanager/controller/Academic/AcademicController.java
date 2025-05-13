package com.example.taskmanager.controller.Academic;

import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.service.AssignmentService;
import com.example.taskmanager.service.EventsService;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/academic")
public class AcademicController {

    @Autowired
    private UserService userService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private EventsService eventsService;

    /**
     * Provides HTML page with a study sessions that are upcoming and also assignments that are
     * within 3 days of deadline or are in progress and also suggested tasks
     * @param model
     * @param principal
     * @return
     */
    @RequestMapping("/dashboard")
    public String dashboard(Model model, Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("studySessions", eventsService.getUserStudySessionsForCurrentDate(user.getStudySessions()));
        model.addAttribute("assignmentTasks", assignmentService.getSuggestedAssignmentTasks(user));
        model.addAttribute("assignments", assignmentService.getAssignmentsCloseToDeadline(user));
        model.addAttribute("assignnmetsInProg", assignmentService.getUserAssignmentsInProgress(user));
        return "academic/dashboard";
    }

    /**
     * Provides HTML page with users assignments
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/assignments")
    public String assignments(Model model, Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("assignments",assignmentService.getUserAssignments(principal));
        return "academic/assignments";
    }

    /**
     * Used to display an assignment via its id
     * @param id
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/assignments/{id}")
    public String assignments(@PathVariable long id,Model model,Principal principal){
        if(!assignmentService.assignmentExistsById(id) || !assignmentService.isOnAssignment(id,principal)){
            return "error";
        }
        MyUser user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("assignments",assignmentService.getUserAssignments(principal));
        model.addAttribute("showAssignment",id);
        return "academic/assignments";
    }

    /**
     * Used to display an assignments task via id
     * @param assignmentId
     * @param taskId
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/assignments/{assignmentId}/tasks/{taskId}")
    public String assignmentsTask(@PathVariable long assignmentId,@PathVariable long taskId, Model model, Principal principal){
        if(!assignmentService.assignmentExistsById(assignmentId) || !assignmentService.assignmentTaskExistsById(taskId) || !assignmentService.isOnAssignment(assignmentId,principal)){
            return "error";
        }
        MyUser user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("assignments",assignmentService.getUserAssignments(principal));
        model.addAttribute("showAssignment",assignmentId);
        model.addAttribute("showTask", taskId);
        return "academic/assignments";
    }

    /**
     * Helps render HTML page with users study session that are during the current dat
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/study-sessions")
    public String studySessions(Model model, Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        model.addAttribute("assignments", assignmentService.getUserAssignmentsInProgress(user));
        model.addAttribute("dateLabel", dateFormat.format(new Date()));
        model.addAttribute("studySessions", eventsService.getUserStudySessionsForCurrentDate(user.getStudySessions()));
        model.addAttribute("user", user);
        return "academic/study-sessions";
    }

    /**
     * Displays details about a study session by provided id
     * @param id
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/study-sessions/{id}")
    public String studySessions(@PathVariable long id, Model model, Principal principal){
        if(!eventsService.studySessionExistsById(id)){
            return "error";
        }
        MyUser user = userService.getUserByEmail(principal.getName());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        model.addAttribute("assignments", assignmentService.getUserAssignmentsInProgress(user));
        model.addAttribute("dateLabel", dateFormat.format(new Date()));
        model.addAttribute("studySessions", eventsService.getUserStudySessionsForCurrentDate(user.getStudySessions()));
        model.addAttribute("user", user);
        model.addAttribute("showSession", id);
        return "academic/study-sessions";
    }
}
