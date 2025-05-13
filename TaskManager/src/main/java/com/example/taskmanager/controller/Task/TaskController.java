package com.example.taskmanager.controller.Task;

import com.example.taskmanager.component.CommonUtils;
import com.example.taskmanager.domain.Task.Category;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.service.CategoryService;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CategoryService catService;

    @Autowired
    private CommonUtils utils;

    /**
     * Provides models that show a users suggested tasks, tasks to start today
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/dashboard")
    private String dashboard(Model model, Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());

        //Get Suggested Tasks
        List<Task> suggestedTasks = taskService.getSuggestedTasks(user);
        List<GroupTask> suggestedGroupTasks = taskService.getSuggestedGroupTask(user);
        List<Object> suggested = new ArrayList<>();
        suggested.addAll(suggestedTasks);
        suggested.addAll(suggestedGroupTasks);
        suggested = utils.getTasksSortedBasedOnImportanceAndUrgency(suggested);

        //Tasks For today
        List<Task> tasksToStart = taskService.getTasksNotStartedToStartToday(user);
        List<GroupTask> groupTasksToStart = taskService.getGroupTasksNotStartedToStartToday(user);
        List<Object> forToday = new ArrayList<>();
        forToday.addAll(tasksToStart);
        forToday.addAll(groupTasksToStart);
        forToday = utils.getTasksSortedBasedOnImportanceAndUrgency(forToday);

        model.addAttribute("suggestedTasks", suggested);
        model.addAttribute("tasksForToday", forToday);
        model.addAttribute("user", user);
        model.addAttribute("categories", user.getCategories());
        return "task/dashboard";
    }

    /**
     * Renders HTML page which shows all of the users tasks
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/personal")
    private String taskPage(Model model, Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        List<Task> tasks = taskService.getUserTasks(principal);
        model.addAttribute("user", user);
        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", user.getCategories());
        return"task/tasks";
    }

    /**
     * Provides HTML page with a category's tasks
     * @param cat
     * @param id
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/category")
    private String categoryTasks(@RequestParam String cat, @RequestParam Long id, Model model, Principal principal){
        Category category = catService.getCategoryById(id);
        if(!catService.categoryExistsById(id) || !catService.isOwner(category,principal)){
            return "error";
        }
        MyUser user = userService.getUserByEmail(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("categories", user.getCategories());
        model.addAttribute("category", category);
        model.addAttribute("categoryTasks", catService.getCategoryTasks(id,principal));
        return "task/tasks";
    }

    /**
     * Helps render all a users group tasks
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/group")
    private String groupTasks(Model model, Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        List<GroupTask> tasks = taskService.getUserGroupTasks(principal);
        model.addAttribute("user", user);
        model.addAttribute("categories", user.getCategories());
        model.addAttribute("groupTasks", tasks);
        return "task/tasks";
    }

    /**
     * Displays a task via id
     * @param id
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/personal/{id}")
    private String getTaskById(@PathVariable Long id, Model model, Principal principal){
        if(!taskService.taskExistsById(id) || !taskService.isOwner(id,principal)){
            return "error";
        }
        MyUser user = userService.getUserByEmail(principal.getName());
        List<Task> tasks = taskService.getUserTasks(principal);
        model.addAttribute("user", user);
        model.addAttribute("showTask", id);
        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", user.getCategories());
        return"task/tasks";
    }

    /**
     * Displays a group task via id
     * @param id
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/group/{id}")
    private String getGroupTaskById(@PathVariable Long id, Model model, Principal principal){
        if(!taskService.groupTaskExistsById(id) || !taskService.isInGroupTask(id,principal)){
            return "error";
        }
        MyUser user = userService.getUserByEmail(principal.getName());
        List<GroupTask> tasks = taskService.getUserGroupTasks(principal);
        model.addAttribute("user", user);
        model.addAttribute("categories", user.getCategories());
        model.addAttribute("groupTasks", tasks);
        model.addAttribute("showTask", id);
        model.addAttribute("categories", user.getCategories());
        return"task/tasks";
    }

    /**
     * Renders page with all the archived tasks that a user has
     * @param model
     * @param principal
     * @return
     */
    @GetMapping("/archived")
    private String archivedTasks(Model model, Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        List<Task> tasks = taskService.getUserArchivedTasks(principal);
        model.addAttribute("user", user);
        model.addAttribute("categories", user.getCategories());
        model.addAttribute("archivedTasks", tasks);
        return "task/tasks";
    }
}
