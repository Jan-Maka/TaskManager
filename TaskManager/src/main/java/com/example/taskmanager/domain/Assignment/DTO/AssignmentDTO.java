package com.example.taskmanager.domain.Assignment.DTO;

import com.example.taskmanager.domain.Events.DTO.StudySessionDTO;
import com.example.taskmanager.domain.File.DTO.FileAttachmentDTO;
import com.example.taskmanager.domain.User.DTO.UserDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DTO used for assignments to display details from API calls
 */
public class AssignmentDTO {
    private long id;

    private long user;

    private String owner;

    private String title;

    private String course;

    private String description;

    private Date startDate;

    private Date endDate;

    private Date created;

    private Date finished;

    private boolean inProgress;

    private boolean complete;

    private int progress;

    private boolean important;

    private boolean urgent;

    private List<FileAttachmentDTO> fileAttachments = new ArrayList<>();

    private List<UserDTO> users = new ArrayList<>();

    private List<AssignmentTaskDTO> tasks = new ArrayList<>();

    private List<StudySessionDTO> sessions = new ArrayList<>();

    public AssignmentDTO() {
    }

    public AssignmentDTO(long id, long user, String owner, String title, String course, String description, Date startDate, Date endDate, Date created, boolean inProgress, boolean complete, int progress, boolean important, boolean urgent) {
        this.id = id;
        this.user = user;
        this.owner = owner;
        this.title = title;
        this.course = course;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.created = created;
        this.inProgress = inProgress;
        this.complete = complete;
        this.progress = progress;
        this.important = important;
        this.urgent = urgent;
    }

    public long getId() {
        return id;
    }

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public List<FileAttachmentDTO> getFileAttachments() {
        return fileAttachments;
    }

    public void setFileAttachments(List<FileAttachmentDTO> fileAttachments) {
        this.fileAttachments = fileAttachments;
    }

    public List<UserDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }

    public List<AssignmentTaskDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<AssignmentTaskDTO> assignmentTasks) {
        this.tasks = assignmentTasks;
    }

    public List<StudySessionDTO> getSessions() {
        return sessions;
    }

    public void setSessions(List<StudySessionDTO> sessions) {
        this.sessions = sessions;
    }
}
