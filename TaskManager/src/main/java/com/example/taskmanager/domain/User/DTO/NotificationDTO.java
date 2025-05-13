package com.example.taskmanager.domain.User.DTO;

import com.example.taskmanager.domain.Assignment.DTO.AssignmentDTO;
import com.example.taskmanager.domain.Assignment.DTO.AssignmentTaskDTO;
import com.example.taskmanager.domain.Events.DTO.StudySessionDTO;
import com.example.taskmanager.domain.Task.DTO.GroupTaskDTO;
import com.example.taskmanager.domain.Task.DTO.TaskDTO;

import java.util.List;

/**
 * NotificationsDTO template for an in application notification
 */
public class NotificationDTO {

    private List<TaskDTO> tasks;

    private List<GroupTaskDTO> groupTasks;

    private List<AssignmentDTO> assignments;

    private List<AssignmentTaskDTO> assignmentTasks;

    private List<StudySessionDTO> studySessions;


    public NotificationDTO(List<TaskDTO> taskDTOS, List<GroupTaskDTO> groupTaskDTOS, List<AssignmentDTO> assignmentDTOS, List<AssignmentTaskDTO> assignmentTaskDTOS, List<StudySessionDTO> studySessionDTOS) {
        this.tasks = taskDTOS;
        this.groupTasks = groupTaskDTOS;
        this.assignments = assignmentDTOS;
        this.assignmentTasks = assignmentTaskDTOS;
        this.studySessions = studySessionDTOS;
    }

    public List<TaskDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDTO> tasks) {
        this.tasks = tasks;
    }

    public List<GroupTaskDTO> getGroupTasks() {
        return groupTasks;
    }

    public void setGroupTasks(List<GroupTaskDTO> groupTasks) {
        this.groupTasks = groupTasks;
    }

    public List<AssignmentDTO> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<AssignmentDTO> assignments) {
        this.assignments = assignments;
    }

    public List<AssignmentTaskDTO> getAssignmentTasks() {
        return assignmentTasks;
    }

    public void setAssignmentTasks(List<AssignmentTaskDTO> assignmentTasks) {
        this.assignmentTasks = assignmentTasks;
    }

    public List<StudySessionDTO> getStudySessions() {
        return studySessions;
    }

    public void setStudySessions(List<StudySessionDTO> studySessions) {
        this.studySessions = studySessions;
    }
}
