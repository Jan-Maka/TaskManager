package com.example.taskmanager.controller;

import com.example.taskmanager.component.CommonUtils;
import com.example.taskmanager.domain.Assignment.AssignmentTask;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.service.AssignmentService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SiteController {

    @Autowired
    private UserService userService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private CommonUtils utils;


    @RequestMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Renders all content for the home page which includes all tasks a user has in progress, close to completion,
     * important.urgent, suggested. Also get details on amount of tasks that need to be started, amount completed today
     * and to start.
     * @param principal
     * @param model
     * @return
     */
    @RequestMapping("/home")
    public String homepage(Principal principal, Model model){
        MyUser user = userService.getUserByEmail(principal.getName());

        //Get Suggested Tasks
        List<Task> suggestedTasks = taskService.getSuggestedTasks(user);
        List<GroupTask> suggestedGroupTasks = taskService.getSuggestedGroupTask(user);
        List<AssignmentTask> suggestedAssignmentTasks = assignmentService.getSuggestedAssignmentTasks(user);
        List<Object> tasks = new ArrayList<>();
        tasks.addAll(suggestedTasks);
        tasks.addAll(suggestedGroupTasks);
        tasks.addAll(suggestedAssignmentTasks);
        tasks = utils.getTasksSortedBasedOnImportanceAndUrgency(tasks);

        //Get important/urgent tasks
        List<Task> importantTasks = taskService.getUrgentImportantTaskForToday(user);
        List<GroupTask> importantGroupTasks = taskService.getUrgentImportantGroupTasksForToday(user);
        List<AssignmentTask> importantAssignmentTasks = assignmentService.getImportantUrgentAssignmentTasksForToday(user);
        List<Object> importantUrgentTasks = new ArrayList<>();
        importantUrgentTasks.addAll(importantTasks);
        importantUrgentTasks.addAll(importantAssignmentTasks);
        importantUrgentTasks.addAll(importantGroupTasks);

        //Get tasks in progress
        List<Task> tasksInProg = taskService.getTasksInProgress(user);
        List<GroupTask> groupTasksInProg = taskService.getGroupTasksInProgress(user);
        List<AssignmentTask> assignmentTasksInProg = assignmentService.getAssignmentTasksInProgress(user);
        List<Object> allTasksInProg = new ArrayList<>();
        allTasksInProg.addAll(tasksInProg);
        allTasksInProg.addAll(groupTasksInProg);
        allTasksInProg.addAll(assignmentTasksInProg);
        allTasksInProg = utils.getTasksSortedBasedOnImportanceAndUrgency(allTasksInProg);

        //Get tasks closest to completion
        List<Task> tasksNearComp = taskService.getTasksCloseToCompletion(user);
        List<GroupTask> groupTasksNearComp = taskService.getGroupTasksCloseToCompletion(user);
        List<AssignmentTask> assignmentTasksNearComp = assignmentService.getAssignmentTasksClosestToCompletion(user);
        List<Object> allTasksNearComp = new ArrayList<>();
        allTasksNearComp.addAll(tasksNearComp);
        allTasksNearComp.addAll(groupTasksNearComp);
        allTasksNearComp.addAll(assignmentTasksNearComp);
        allTasksNearComp = utils.getTasksSortedBasedOnImportanceAndUrgency(allTasksNearComp);

        //Get tasks to be completed today
        List<Task> tasksToComp = taskService.getTasksToCompleteToday(user);
        List<GroupTask> groupTasksToComp = taskService.getGroupTasksToCompleteToday(user);
        List<AssignmentTask> assignmentTasksToComp = assignmentService.getAssignmentTasksToCompleteToday(user);
        int tasksToCompToday = tasksToComp.size()+groupTasksToComp.size()+assignmentTasksToComp.size();

        //Get tasks to start for today
        List<Task> tasksToStart = taskService.getTasksNotStartedToStartToday(user);
        List<GroupTask> groupTasksToStart = taskService.getGroupTasksNotStartedToStartToday(user);
        List<AssignmentTask> assignmentTasksToStart = assignmentService.getAssignmentTasksToBeStartedToday(user);
        int tasksToStartToday = tasksToStart.size()+groupTasksToStart.size()+assignmentTasksToStart.size();

        //Get tasks completed today
        List<Task> taskComp = taskService.getTasksCompletedToday(user);
        List<GroupTask> groupTasksComp = taskService.getGroupTasksCompletedToday(user);
        List<AssignmentTask> assignmentTasksComp = assignmentService.getAssignmentTasksCompletedToday(user);
        int tasksCompToday = taskComp.size()+groupTasksComp.size()+assignmentTasksComp.size();

        model.addAttribute("user",user);
        model.addAttribute("suggestedTasks", tasks);
        model.addAttribute("importantUrgentTasks", importantUrgentTasks);
        model.addAttribute("tasksInProg", allTasksInProg);
        model.addAttribute("tasksNearComp", allTasksNearComp);
        model.addAttribute("tasksForToday", tasksToCompToday);
        model.addAttribute("tasksToStart", tasksToStartToday);
        model.addAttribute("tasksComp",tasksCompToday);
        model.addAttribute("firstLogin", userService.isFirstLoginToday(user));
        return "home";
    }
}