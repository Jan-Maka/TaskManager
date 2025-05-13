package com.example.taskmanager.domain.Task;

import com.example.taskmanager.domain.File.FileAttachment;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@MappedSuperclass
public class TaskBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private boolean important;

    private boolean urgent;

    private short workload;

    private short mood;

    @Column(length = 500)
    private String description;

    private Date startDate;

    private Date endDate;

    private int progress;

    @CreationTimestamp
    private Date created;

    private Date finished;

    private Date modified;

    private boolean complete;

    private boolean inProgress;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileAttachment> fileAttachments = new ArrayList<>();

    public TaskBaseEntity() {
    }

    public TaskBaseEntity(String title, boolean important, boolean urgent, short workload, short mood, String description, Date startDate, Date endDate, int progress, Date modified, boolean complete, boolean inProgress) {
        this.title = title;
        this.important = important;
        this.urgent = urgent;
        this.workload = workload;
        this.mood = mood;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
        this.modified = modified;
        this.complete = complete;
        this.inProgress = inProgress;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getId() {
        return id;
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

    public List<FileAttachment> getFileAttachments() {
        return fileAttachments;
    }

    public void setFileAttachments(List<FileAttachment> fileAttachments) {
        this.fileAttachments = fileAttachments;
    }

    public String getStartDateString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String formattedDateTime = dateFormat.format(startDate);
        return formattedDateTime;
    }

    public String getEndDateString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String formattedDateTime = dateFormat.format(endDate);
        return formattedDateTime;
    }

    public String getFinishDateString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String formattedDateTime = dateFormat.format(finished);
        return formattedDateTime;
    }

    public String getType(){
        if(urgent && important){
            return "Urgent/Important 🔥";
        }else if(urgent){
            return "Urgent ⏰";

        }else if(important){
            return "Important ‼";
        }else{
            return "Not important or Urgent 🛏️";
        }
    }

    public String getStatus(){
        if(inProgress){
            return "🚧";
        }else if(complete){
            return "✅";
        }else{
            return "❌";
        }
    }

    public boolean isOverdue(){
        if(complete){
            return false;
        }
        return endDate.before(new Date());
    }
}
