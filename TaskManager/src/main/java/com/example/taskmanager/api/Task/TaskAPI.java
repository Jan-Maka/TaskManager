package com.example.taskmanager.api.Task;

import com.example.taskmanager.domain.Task.Category;
import com.example.taskmanager.domain.Task.DTO.CategoryDTO;
import com.example.taskmanager.domain.Task.DTO.GroupTaskDTO;
import com.example.taskmanager.domain.Task.DTO.TaskDTO;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.MyUser;
import com.example.taskmanager.service.CategoryService;
import com.example.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.management.loading.PrivateClassLoader;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/task")
public class TaskAPI {

    @Autowired
    private CategoryService catService;

    @Autowired
    private TaskService taskService;

    /**
     * API endpoint for getting a users categories
     * @param principal
     * @return
     */
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories(Principal principal){
        List<Category> categories = catService.getUserCategories(principal);
        if(categories.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<CategoryDTO> categoryDTOS = catService.getUserCategoriesDTO(categories);
        return new ResponseEntity<>(categoryDTOS,HttpStatus.OK);
    }

    /**
     * API endpoint for getting category via id
     * @param id
     * @param principal
     * @return
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id,Principal principal){
        if(catService.categoryExistsById(id)){
            Category category = catService.getCategoryById(id);
            return new ResponseEntity<>(catService.createCategoryDTO(category),HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for creating a category for user and checks if its exists
     * @param cat
     * @param principal
     * @return
     */
    @PostMapping("/add/category/{cat}")
    public ResponseEntity<?> addCategory(@PathVariable String cat, Principal principal){
        if(catService.categoryExistsForUser(cat,principal)){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(catService.createCategory(cat,principal),HttpStatus.OK);
    }

    /**
     * API enpoint for deleting a category via id
     * @param id
     * @param principal
     * @return
     */
    @DeleteMapping("/delete/category/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable long id,Principal principal){
        if(!catService.isOwner(catService.getCategoryById(id), principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        if (catService.categoryExistsById(id)) {
            catService.deleteCategoryById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Gets all users tasks
     * @param principal
     * @return
     */
    @GetMapping("/user/tasks")
    public ResponseEntity<?> getAllUserTasks(Principal principal){
        List<Task> tasks = taskService.getUserTasks(principal);
        if(tasks.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<TaskDTO> taskDTOS = taskService.getUserTasksDTO(tasks);
        return new ResponseEntity<>(taskDTOS,HttpStatus.OK);
    }

    /**
     * API endpoint for getting details about a task via id
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserTask(@PathVariable long id){
        if(taskService.taskExistsById(id)){
            Task task = taskService.getTaskById(id);
            TaskDTO taskDTO = taskService.createTaskDTO(task);
            return new ResponseEntity<>(taskDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for creating a task from HTML form including file attachments with it
     * @param taskDTO
     * @param files
     * @return
     * @throws IOException
     */
    @PostMapping("")
    public ResponseEntity<?> createTask(@RequestPart(name = "taskDTO") TaskDTO taskDTO, @RequestPart(name = "fileAttachments", required = false) List<MultipartFile> files) throws IOException {
        taskService.CreateTask(taskDTO,files);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * API endpoint for deleting a task via id but first checks if the user owns it
     * @param id
     * @param principal
     * @return
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable long id,Principal principal) {
        if(!taskService.isOwner(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        if (taskService.taskExistsById(id)) {
            taskService.deleteTaskById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for updating task details from HTML form and adding any additional file attachments
     * @param id
     * @param taskDTO
     * @param files
     * @param principal
     * @return
     * @throws IOException
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> editTask(@PathVariable long id, @RequestPart(name = "taskDTO") TaskDTO taskDTO,@RequestPart(name = "fileAttachments", required = false) List<MultipartFile> files, Principal principal) throws IOException {
        if(!taskService.isOwner(id, principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }

        if(taskService.taskExistsById(id)){
            taskService.updateTask(taskDTO,files);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for archiving a personal task
     * @param id
     * @param archive
     * @param principal
     * @return
     */
    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<?> archiveTask(@PathVariable Long id, @RequestParam boolean archive ,Principal principal){
        if(!taskService.taskExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(taskService.isOwner(id,principal)){
            taskService.setArchiveTask(id,archive);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
    }

    /**
     * API endpoint for searching through all tasks matching the title query given
     * @param search
     * @param principal
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchAllTasks(@RequestParam String search, Principal principal){
        List<TaskDTO> tasks = taskService.searchAllTasks(search,principal);
        if(!tasks.isEmpty()){
            return new ResponseEntity<>(tasks,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


    /**
     * API endpoint for getting all tasks that are archived
     * @param principal
     * @return
     */
    @GetMapping("/archived")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<?> getUserArchivedTasks(Principal principal){
        List<Task> tasks = taskService.getUserArchivedTasks(principal);
        if(tasks.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(taskService.getUserTasksDTO(tasks),HttpStatus.OK);
    }

    /**
     * API endpoint for searching archived tasks based on title
     * @param query
     * @param principal
     * @return
     */
    @GetMapping("/archived/search")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<?> searchArchivedTasks(@RequestParam String query, Principal principal){
        List<TaskDTO> tasks = taskService.getArchivedTaskSearchResult(query, principal);
        if(!tasks.isEmpty()){
            return new ResponseEntity<>(tasks,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for searching tasks in a category via their title
     * @param search
     * @param catId
     * @return
     */
    @GetMapping("/search/category")
    public ResponseEntity<?> searchTasksByCategory(@RequestParam String search, @RequestParam long catId){
        List<TaskDTO> tasks = taskService.searchTasksByCategory(search,catId);
        if(!tasks.isEmpty()){
            return new ResponseEntity<>(tasks,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for creating a group task
     * @param groupTaskDTO
     * @param files
     * @return
     * @throws IOException
     */
    @PostMapping("/group")
    public ResponseEntity<?> createGroupTask(@RequestPart(name = "groupTaskDTO") GroupTaskDTO groupTaskDTO, @RequestPart(name = "fileAttachments", required = false) List<MultipartFile> files) throws IOException {
        taskService.createGroupTask(groupTaskDTO,files);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * API endpoint for getting all the group tasks that a user is a part of.
     * @param principal
     * @return
     */
    @GetMapping("/group")
    public ResponseEntity<?> getUserGroupTasks(Principal principal){
        List<GroupTask> groupTasks = taskService.getUserGroupTasks(principal);
        if(groupTasks.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(taskService.getUserGroupTasksDTO(groupTasks),HttpStatus.OK);
    }

    /**
     * Gets a group task details via id if it exists
     * @param id
     * @return
     */
    @GetMapping("/group/{id}")
    public ResponseEntity<?> getUserGroupTask(@PathVariable long id){
        if(taskService.groupTaskExistsById(id)){
            GroupTask groupTask = taskService.getGroupTaskById(id);
            return new ResponseEntity<>(taskService.createGroupTaskDTO(groupTask),HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for deleting a group task via id, first checks if the user is the owner deleting
     * @param id
     * @param principal
     * @return
     */
    @DeleteMapping("/group/{id}")
    public ResponseEntity<?> deleteGroupTask(@PathVariable long id, Principal principal){
        if(!taskService.isGroupTaskOwner(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        if(taskService.groupTaskExistsById(id)){
            taskService.deleteGroupTaskById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

    /**
     * API endpoint for group task search which finds tasks based on the title from search query
     * @param search the query used to find group tasks matching title
     * @param principal
     * @return
     */
    @GetMapping("/group/search")
    public ResponseEntity<?> searchGroupTasks(@RequestParam String search, Principal principal){
        List<GroupTaskDTO> result = taskService.searchGroupTasks(search,principal);
        if(!result.isEmpty()){
            return new ResponseEntity<>(result,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for updating a group task details
     * @param id
     * @param groupTaskDTO
     * @param files
     * @param principal
     * @return
     * @throws IOException
     */
    @PutMapping("/group/{id}")
    public ResponseEntity<?> editGroupTask(@PathVariable long id,  @RequestPart(name = "groupTaskDTO") GroupTaskDTO groupTaskDTO,@RequestPart(name = "fileAttachments", required = false) List<MultipartFile> files, Principal principal) throws IOException {
        if(!taskService.isInGroupTask(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        if(taskService.groupTaskExistsById(id)){
            taskService.updateGroupTask(groupTaskDTO,files);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for a user leaving a task if they are not the owner
     * @param id
     * @param principal
     * @return
     */
    @DeleteMapping("/group/leave/{id}")
    public ResponseEntity<?> userLeaveTask(@PathVariable long id, Principal principal){
        if(taskService.isInGroupTask(id,principal)){
           if(taskService.groupTaskExistsById(id)){
               taskService.removeUserFromGroupTask(id,principal);
               return new ResponseEntity<>(HttpStatus.OK);
           }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * Gets the number of tasks completed a day in the past 7 day
     * @param id
     * @return
     */
    @GetMapping("/user/{id}/num-completed")
    public ResponseEntity<?> getNumberOfTasksCompletedThisWeek(@PathVariable Long id){
        return new ResponseEntity<>(taskService.getNumberOfTasksCompletedForWeek(id),HttpStatus.OK);
    }

    /**
     * Gets the number of group tasks completed a day in the past 7 days
     * @param id
     * @return
     */
    @GetMapping("/group/user/{id}/num-completed")
    public ResponseEntity<?> gerNumberOfGroupTasksCompletedThisWeek(@PathVariable Long id){
        return new ResponseEntity<>(taskService.getNumberOfGroupTasksCompletedOverWeek(id),HttpStatus.OK);
    }

    /**
     * API endpoint for getting group tasks that don't have a group chat yet
     * @param query
     * @param principal
     * @return
     */
    @GetMapping("/group/search/available-tasks-for-chat")
    public ResponseEntity<?> getGroupTasksAvailableForChat(@RequestParam String query,Principal principal){
        List<GroupTaskDTO> tasks = taskService.getGroupTasksAvailableForChat(query,principal);
        if(!tasks.isEmpty()){
            return new ResponseEntity<>(tasks,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for updating a group or personal task status
     * @param id
     * @param inProgress
     * @param complete
     * @param type
     * @param principal
     * @return
     */
    @PatchMapping("/{id}/set-status")
    public ResponseEntity<?> setAssignmentTaskStatus(@PathVariable Long id, @RequestParam boolean inProgress,@RequestParam boolean complete,@RequestParam String type, Principal principal){
        switch (type){
            case "task":
                if(!taskService.taskExistsById(id)){
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                if(!taskService.isOwner(id,principal)){
                    return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
                }
                taskService.setTaskStatus(id,inProgress,complete);
                return new ResponseEntity<>(HttpStatus.OK);
            case "group":
                if(!taskService.groupTaskExistsById(id)){
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                if(!taskService.isInGroupTask(id,principal)){
                    return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
                }
                taskService.setGroupTaskStatus(id, inProgress, complete);
                return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}