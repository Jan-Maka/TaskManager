package com.example.taskmanager.domain.Task.DTO;

import com.example.taskmanager.domain.File.DTO.FileAttachmentDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * Contains details about a task for API calls
 */
public class TaskDTO {
    private long id;

    private long user;

    private String title;

    private boolean important;

    private boolean urgent;

    private short workload;

    private short mood;

    private String description;

    private long category;

    private String categoryName;

    private Date startDate;

    private Date endDate;

    private int progress;

    private Date created;

    private Date finished;

    private Date modified;

    private boolean complete;

    private boolean inProgress;

    private List<FileAttachmentDTO> fileAttachments = new ArrayList<>();

    private String taskType = "task";

    private boolean archive;

    public TaskDTO() {
    }

    public TaskDTO(long id, long user, String title, boolean important, boolean urgent, short workload, short mood, String description, long category,String categoryName ,Date startDate, Date endDate, int progress, Date created, Date modified, boolean complete, boolean inProgress, boolean archive) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.important = important;
        this.urgent = urgent;
        this.workload = workload;
        this.mood = mood;
        this.description = description;
        this.category = category;
        this.categoryName = categoryName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
        this.created = created;
        this.modified = modified;
        this.complete = complete;
        this.inProgress = inProgress;
        this.archive = archive;
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public long getCategory() {
        return category;
    }

    public void setCategory(long category) {
        this.category = category;
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

    public String getTaskType() {
        return taskType;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }
}