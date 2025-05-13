package com.example.taskmanager.api.Events;

import com.example.taskmanager.domain.Events.DTO.StudySessionDTO;
import com.example.taskmanager.domain.Events.StudySession;
import com.example.taskmanager.service.AssignmentService;
import com.example.taskmanager.service.EventsService;
import com.example.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventsAPI {

    @Autowired
    private EventsService eventsService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private AssignmentService assignmentService;

    /**
     * API endpoint for creating study sessions
     * @param studySessionDTO
     * @return
     */
    @PostMapping("/study-sessions")
    public ResponseEntity<?> createStudySession(@RequestBody StudySessionDTO studySessionDTO){
        return new ResponseEntity<>(eventsService.createStudySession(studySessionDTO),HttpStatus.CREATED);
    }

    /**
     * API endpoint for getting information about a study-session via id if it exists
     * @param id of study session
     * @return 404 if not found or studySessionDTO if with status code of 200
     */
    @GetMapping("/study-sessions/{id}")
    public ResponseEntity<?> getStudySession(@PathVariable long id){
        if(!eventsService.studySessionExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        StudySession studySession = eventsService.getStudySessionById(id);
        return new ResponseEntity<>(eventsService.createStudySessionDTO(studySession),HttpStatus.OK);
    }

    /**
     * API endpoint for getting user study sessions DTO for the date-range provided
     * @param date1
     * @param date2
     * @param principal
     * @return
     */
    @GetMapping("/study-sessions/for-dates")
    public ResponseEntity<?> getStudySessionsForDates(@RequestParam Date date1,@RequestParam Date date2,Principal principal){
        List<StudySessionDTO> studySessionDTOs = eventsService.getStudySessionsInDateRange(principal,date1,date2);
        if(studySessionDTOs.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(studySessionDTOs, HttpStatus.OK);
    }

    /**
     * Deletes a study session via id
     * @param id
     * @param principal
     * @return
     */
    @DeleteMapping("/study-sessions/{id}")
    public ResponseEntity<?> deleteStudySession(@PathVariable long id, Principal principal){
        if(!eventsService.studySessionExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(!eventsService.isStudySessionOrganiser(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        eventsService.deleteStudySessionById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * API endpoint for updating a study session details if it exists and the user is the organiser
     * @param id
     * @param studySessionDTO
     * @param principal
     * @return
     */
    @PutMapping("/study-sessions/{id}")
    public ResponseEntity<?> updateStudySession(@PathVariable long id ,@RequestBody StudySessionDTO studySessionDTO, Principal principal){
        if(!eventsService.studySessionExistsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if(!eventsService.isStudySessionOrganiser(id,principal)){
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        eventsService.updateStudySession(studySessionDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Gets number of study sessions a user has throughout the next 7 days
     * @param principal
     * @return
     */
    @GetMapping("/study-sessions/count")
    public ResponseEntity<?> getNumberOfStudySessionsForWeek(Principal principal){
        return new ResponseEntity<>(eventsService.getNumberOfStudySessionForWeek(principal),HttpStatus.OK);
    }

    /**
     * Get all of the users study sessions
     * @param principal
     * @return
     */
    @GetMapping("/study-sessions")
    public ResponseEntity<?> getUserStudySessions(Principal principal){
        return new ResponseEntity<>(eventsService.getAllUserStudySessionsDTO(principal),HttpStatus.OK);

    }

    /**
     * Updates the date provided from the calendar on a resize or drag and drop event.
     * @param start
     * @param end
     * @param type
     * @param id
     * @param principal
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PatchMapping("/calendar")
    public ResponseEntity<?> updateEventDate(@RequestParam Date start,@RequestParam Date end, @RequestParam String type,@RequestParam Long id,Principal principal){
        switch(type){
            case "task":
                if(taskService.taskExistsById(id) && taskService.isOwner(id,principal)){
                    taskService.updateTaskDates(id,start,end);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                break;
            case "groupTask":
                if(taskService.groupTaskExistsById(id) && taskService.isInGroupTask(id,principal)){
                    taskService.updateGroupTaskDates(id,start,end);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                break;
            case "assignmentTask":
                if(assignmentService.assignmentTaskExistsById(id) && assignmentService.isOnAssignmentTask(id,principal)){
                    assignmentService.updateAssignmentTaskDates(id,start,end);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                break;
            case "assignment":
                if(assignmentService.assignmentExistsById(id) && assignmentService.isOnAssignment(id,principal)){
                    assignmentService.updateAssignmentDates(id,start,end);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                break;
            case "session":
                if(eventsService.studySessionExistsById(id) && eventsService.isStudySessionOrganiser(id,principal)){
                    eventsService.updateStudySessionDates(id,start,end);
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                break;
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
