package com.example.taskmanager.service;

import com.example.taskmanager.domain.Assignment.DTO.AssignmentDTO;
import com.example.taskmanager.domain.Assignment.DTO.AssignmentTaskDTO;
import com.example.taskmanager.domain.Events.DTO.StudySessionDTO;
import com.example.taskmanager.domain.Task.DTO.GroupTaskDTO;
import com.example.taskmanager.domain.Task.DTO.TaskDTO;
import com.example.taskmanager.domain.User.AccountSettings;
import com.example.taskmanager.domain.User.DTO.NotificationDTO;
import com.example.taskmanager.domain.User.MyUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private UserService userService;
    @Autowired
    private TaskService taskService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private EventsService eventsService;

    /**
     * Creates an in system notification that holds task/assignments/study sessions
     * that are to begin within 30 minutes
     * @param principal
     * @return
     */
    public NotificationDTO getUserNotifications(Principal principal){
        MyUser user = userService.getUserByEmail(principal.getName());
        AccountSettings accountSettings = user.getAccountSettings();
        List<TaskDTO> tasks = accountSettings.isSendTaskNotifications() ? taskService.getTasksToStartIn30Minutes(user) : new ArrayList<>();
        List<GroupTaskDTO> groupTasks = accountSettings.isSendTaskNotifications() ? taskService.getGroupTasksToStartIn30Minutes(user) : new ArrayList<>();
        List<AssignmentDTO> assignments = accountSettings.isSendAssignmentNotifications() ? assignmentService.getAssignmentToStartWithin30Minutes(user) : new ArrayList<>();
        List<AssignmentTaskDTO> assignmentTasks = accountSettings.isSendAssignmentNotifications() ? assignmentService.getAssignmentTasksToStartWithin30Minutes(user) : new ArrayList<>();
        List<StudySessionDTO> studySessions = accountSettings.isSendStudySessionNotifications() ? eventsService.getStudySessionsThatBeginIn30Minutes(user) : new ArrayList<>();
        return new NotificationDTO(tasks,groupTasks,assignments,assignmentTasks,studySessions);
    }
}
