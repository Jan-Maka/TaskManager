package com.example.taskmanager.domain.Assignment.DTO;

import com.example.taskmanager.domain.File.DTO.FileAttachmentDTO;
import com.example.taskmanager.domain.User.DTO.UserDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * DTO used for assignment tasks to display details from API calls
 */
public class AssignmentTaskDTO {
    private long id;

    private long owner;

    private String title;

    private boolean important;

    private boolean urgent;

    private short workload;

    private short mood;

    private String description;

    private Date startDate;

    private Date endDate;

    private int progress;

    private Date created;

    private Date finished;

    private Date modified;

    private boolean complete;

    private boolean inProgress;

    private List<FileAttachmentDTO> fileAttachments = new ArrayList<>();

    private List<UserDTO> users = new ArrayList<>();

    private long assignment;

    private String assignmentName;

    public AssignmentTaskDTO() {
    }

    public AssignmentTaskDTO(long owner, String title, boolean important, boolean urgent, short workload, short mood, String description, Date startDate, Date endDate, int progress, boolean complete, boolean inProgress, List<UserDTO> users, long assignment) {
        this.owner = owner;
        this.title = title;
        this.important = important;
        this.urgent = urgent;
        this.workload = workload;
        this.mood = mood;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
        this.complete = complete;
        this.inProgress = inProgress;
        this.users = users;
        this.assignment = assignment;
    }

    public AssignmentTaskDTO(long id, long owner, String title, boolean important, boolean urgent, short workload, short mood, String description, Date startDate, Date endDate, int progress, Date created, Date modified, boolean complete, boolean inProgress, long assignment, String assignmentName) {
        this.id = id;
        this.owner = owner;
        this.title = title;
        this.important = important;
        this.urgent = urgent;
        this.workload = workload;
        this.mood = mood;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
        this.created = created;
        this.modified = modified;
        this.complete = complete;
        this.inProgress = inProgress;
        this.assignment = assignment;
        this.assignmentName = assignmentName;
    }

    public long getId() {
        return id;
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

    public short getWorkload() {
        return workload;
    }

    public void setWorkload(short workload) {
        this.workload = workload;
    }

    public short getMood() {
        return mood;
    }

    public void setMood(short mood) {
        this.mood = mood;
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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
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

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
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

    public long getAssignment() {
        return assignment;
    }

    public void setAssignment(long assignment) {
        this.assignment = assignment;
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public void setAssignmentName(String assignmentName) {
        this.assignmentName = assignmentName;
    }
}
