package com.example.taskmanager.api.Assignment;

import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Assignment.AssignmentTask;
import com.example.taskmanager.domain.Assignment.DTO.AssignmentDTO;
import com.example.taskmanager.domain.Assignment.DTO.AssignmentTaskDTO;
import com.example.taskmanager.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentAPI {

    @Autowired
    private AssignmentService assignmentService;

    /**
     * Handles the creation of an assignment API call.
     * @param assignmentDTO json body containing information about an assignment a user has inputted
     * @param files List of file attachments added onto the assignment
     * @return Status 201 letting ajax call now it is created
     * @throws IOException
     */
    @PostMapping("")
    public ResponseEntity<?> createAssignment(@RequestPart(name = "assignmentDTO")AssignmentDTO assignmentDTO, @RequestPart(value = "fileAttachments",required = false) List<MultipartFile> files) throws IOException {
        assignmentService.createAssignment(assignmentDTO,files);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * API end-point for getting a users assignments
     * @param principal gets current logged in user
     * @return Either a 404 indicating they have no assignments or a 200 with a list of assignment DTOs.
     */
    @GetMapping("")
    public ResponseEntity<?> getUserAssignments(Principal principal){
        List<Assignment> assignments = assignmentService.getUserAssignments(principal);
        if(assignments.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(assignmentService.getUserAssignmentsDTO(assignments),HttpStatus.OK);
    }

    /**
     * Endpoint for getting an assigment via ID
     * @param id the id of the assignment thats being accessed
     * @return Either 404 if not found or 200 and the assignment DTO body
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAssignment(@PathVariable long id){
        if(assignmentService.assignmentExistsById(id)){
            Assignment assignment = assignmentService.getAssignmentById(id);
            return new ResponseEntity<>(assignmentService.createAssignmentDTO(assignment),HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for filtering assignments for user via user search query.
     * @param query the string used filtering.
     * @param principal the current user logged in.
     * @return 200 and list of assignment DTOs matching results or a 400 if no results are found
     */
    @GetMapping("/search")
    public ResponseEntity<?> getAssignmentSearchResults(@RequestParam String query,Principal principal){
        List<AssignmentDTO> assignmentDTOS = assignmentService.getAssignmentsFromSearch(query,principal);
        if(!assignmentDTOS.isEmpty()){
            return new ResponseEntity<>(assignmentDTOS,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for deleting an assigment via ID checks first if the user logged in is
     * indeed the owner of the assignment
     * @param id of the assignment to be deleted
     * @param principal
     * @return 400 if not found via ID/ 406 if the user trying to delete assignment is not the owner/ 200 to indicate it has been deleted
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable long id, Principal principal){
        if(!assignmentService.assignmentExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(!assignmentService.isOwnerOfAssignment(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        assignmentService.deleteAssignmentById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * API endpoint for creating a task for an assignment
     * @param id of the assignment will be associated with
     * @param assignmentTaskDTO json body containing information about a task a user has inputted
     * @param files file attachments for the task
     * @return 201 to indicate it has been created or a 406 to indicate that task cannot be created as the assignment doesn't exist.
     * @throws IOException
     */
    @PostMapping("/{id}/create-task")
    public ResponseEntity<?> createAssignmentTask(@PathVariable long id, @RequestPart(name = "assignmentTaskDTO")AssignmentTaskDTO assignmentTaskDTO, @RequestPart(value = "fileAttachments",required = false) List<MultipartFile> files) throws IOException {
        if(assignmentService.assignmentExistsById(id)){
            List<AssignmentTaskDTO> tasks = assignmentService.createAssignmentTask(assignmentTaskDTO,files);
            return new ResponseEntity<>(tasks,HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
    }

    /**
     * API endpoint for getting users on an assignment
     * @param id of the assignment
     * @return 200 and users DTO on an assignment or 404 if assignment doesn't exist
     */
    @GetMapping("/{id}/users")
    public ResponseEntity<?> getAssignmentUsers(@PathVariable long id){
        if(assignmentService.assignmentExistsById(id)){
            return new ResponseEntity<>(assignmentService.getUsersDTOOnAssignment(id),HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for editing an assignment
     * @param id of assignment to be edited
     * @param assignmentDTO body of edited assignment DTO
     * @param files added file attachments for assignment
     * @param principal current user logged in
     * @return 404 if assignment with id doesn't exist/406 if user is not part of assignment/ 200 indicate assignment has been updated
     * @throws IOException
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> editAssignment(@PathVariable long id, @RequestPart(name = "assignmentDTO")AssignmentDTO assignmentDTO, @RequestPart(value = "fileAttachments",required = false) List<MultipartFile> files, Principal principal) throws IOException {
        if(assignmentService.assignmentExistsById(id)){
            if(!assignmentService.isOnAssignment(id,principal)){
                return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            }
            assignmentService.updateAssignment(assignmentDTO,files);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for getting a assignment task via ID
     * @param id of task to be search for
     * @return 404 if not found by ID/ 200 and task DTO
     */
    @GetMapping("/tasks/{id}")
    public ResponseEntity<?> getAssignmentTask(@PathVariable long id){
        if(assignmentService.assignmentTaskExistsById(id)){
            AssignmentTask assignmentTask = assignmentService.getAssignmentTaskById(id);
            return new ResponseEntity<>(assignmentService.createAssignmentTaskDTO(assignmentTask),HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * API endpoint for deleting a task via id
     * @param id of task to be deleted
     * @param principal current user logged in
     * @return 404 if not found/ 406 if user is not owner of assignment task/ 200 indicate it has been deleted
     */
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteAssignmentTask(@PathVariable long id, Principal principal){
        if(!assignmentService.assignmentTaskExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(!assignmentService.isOwnerOfAssignmentTask(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        assignmentService.deleteAssignmentTask(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * API endpoint for updating an assignment task
     * @param id of task to be updated
     * @param assignmentTaskDTO json body of updated task details
     * @param files files to be attached to task
     * @param principal current user logged in
     * @return 404 if not found via ID/ 406 if user is not on the task/ 200 indicate it has been updated
     * @throws IOException
     */
    @PutMapping("/tasks/{id}")
    public ResponseEntity<?> updateAssignmentTask(@PathVariable long id,@RequestPart(name = "assignmentTaskDTO")AssignmentTaskDTO assignmentTaskDTO, @RequestPart(value = "fileAttachments",required = false) List<MultipartFile> files ,Principal principal) throws IOException {
        if(!assignmentService.assignmentTaskExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(!assignmentService.isOnAssignmentTask(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        assignmentService.updateAssignmentTask(assignmentTaskDTO,files);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * API endpoint for a user leaving an assignment
     * @param id of assignment a user is leaving
     * @param principal current user logged in
     * @return 404 if assignment not found via ID/ 406 if user is not a user of assignment/ 200 indicate the user has successfully left
     */
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @DeleteMapping("/{id}/remove-user")
    public ResponseEntity<?> removeUserFromAssignment(@PathVariable long id,Principal principal){
        if(!assignmentService.assignmentExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(!assignmentService.isOnAssignment(id,principal) || assignmentService.isOwnerOfAssignment(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        assignmentService.removeUserFromAssignment(id, principal);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * API endpoint for user leaving assignment task unless they are the owner
     * @param id of user is leaving
     * @param principal current user logged in
     * @return 404 if task not found via ID/ 406 if user is not a user of task/ 200 indicate the user has successfully left
     */
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @DeleteMapping("/tasks/{id}/remove-user")
    public ResponseEntity<?> removeUserFromAssignmentTask(@PathVariable long id, Principal principal){
        if(!assignmentService.assignmentTaskExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(!assignmentService.isOnAssignmentTask(id,principal) || assignmentService.isOwnerOfAssignmentTask(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        assignmentService.removeUserFromAssignmentTask(id,principal);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * API endpoint for updating assignment status complete/in progress/not complete
     * @param id of assignment
     * @param inProgress boolean for if its in progress
     * @param complete boolean for if its complete
     * @param principal current user logged in
     * @return 404 if not found via ID/ 406 if user is not on assignment/ 200 indicating it has been updated
     */
    @PatchMapping("/set-status/{id}")
    public ResponseEntity<?> setAssignmentStatus(@PathVariable long id, @RequestParam boolean inProgress,@RequestParam boolean complete, Principal principal){
        if(!assignmentService.assignmentExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(!assignmentService.isOnAssignment(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        assignmentService.setAssignmentStatus(id,inProgress,complete);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * API endpoint for updating assignment status complete/in progress/not complete
     * @param id of task
     * @param inProgress boolean for if its in progress
     * @param complete boolean for if its complete
     * @param principal current user logged in
     * @return 404 if not found via ID/ 406 if user is not on task/ 200 indicating it has been updated
     */
    @PatchMapping("/tasks/set-status/{id}")
    public ResponseEntity<?> setAssignmentTaskStatus(@PathVariable long id, @RequestParam boolean inProgress,@RequestParam boolean complete, Principal principal){
        if(!assignmentService.assignmentTaskExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(!assignmentService.isOnAssignmentTask(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        assignmentService.setAssignmentTaskStatus(id,inProgress,complete);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Get number of assignment tasks a user has completed in the past 7 days
     * @param id of user to be checked
     * @return List of integers for assignments completed
     */
    @GetMapping("/tasks/user/{id}/num-completed")
    public ResponseEntity<?> getNumberOfTasksCompletedThisWeek(@PathVariable Long id){
        return new ResponseEntity<>(assignmentService.getNumberOfAssignmentTasksCompletedForWeek(id),HttpStatus.OK);
    }

    /**
     * Gets available assignments for chat
     * @param query filter assignments by title from query
     * @param principal current user logged in
     * @return List of assignment DTOs for available assignments
     */
    @PreAuthorize("hasRole('ROLE_MEMBER')")
    @GetMapping("/search/available-assignments-for-chat/")
    public ResponseEntity<?> getAssignmentsAvailableForChat(@RequestParam String query, Principal principal){
        List<AssignmentDTO> assignmentDTOS = assignmentService.getAssignmentsAvailableForChat(query,principal);
        if(!assignmentDTOS.isEmpty()){
            return new ResponseEntity<>(assignmentDTOS,HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
