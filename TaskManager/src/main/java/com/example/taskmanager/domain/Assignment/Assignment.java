package com.example.taskmanager.domain.Assignment;

import com.example.taskmanager.domain.Events.StudySession;
import com.example.taskmanager.domain.File.FileAttachment;
import com.example.taskmanager.domain.Messaging.GroupChat;
import com.example.taskmanager.domain.User.MyUser;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Assignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private MyUser owner;

    private String title;

    private String course;

    @Column(length = 1000)
    private String description;

    private Date startDate;

    private Date endDate;

    @CreationTimestamp
    private Date created;

    private Date finished;

    private boolean inProgress;

    private boolean complete;

    private int progress;

    private boolean important;

    private boolean urgent;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileAttachment> fileAttachments = new ArrayList<>();

    @ManyToMany
    private List<MyUser> users = new ArrayList<>();

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<AssignmentTask> assignmentTasks = new ArrayList<>();

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL,orphanRemoval = true)
    private List<StudySession> studySessions = new ArrayList<>();

    @OneToOne(mappedBy = "assignment", cascade = CascadeType.REMOVE)
    private GroupChat groupChat;

    public Assignment() {
    }

    public Assignment(MyUser owner, String title, String course, String description, Date startDate, Date endDate, boolean inProgress, boolean complete, int progress, boolean important, boolean urgent) {
        this.owner = owner;
        this.title = title;
        this.course = course;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.inProgress = inProgress;
        this.complete = complete;
        this.progress = progress;
        this.important = important;
        this.urgent = urgent;
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public List<FileAttachment> getFileAttachments() {
        return fileAttachments;
    }

    public void setFileAttachments(List<FileAttachment> fileAttachments) {
        this.fileAttachments = fileAttachments;
    }

    public List<MyUser> getUsers() {
        return users;
    }

    public void setUsers(List<MyUser> users) {
        this.users = users;
    }

    public Date getCreated() {
        return created;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    public List<AssignmentTask> getAssignmentTasks() {
        return assignmentTasks;
    }

    public void setAssignmentTasks(List<AssignmentTask> assignmentTasks) {
        this.assignmentTasks = assignmentTasks;
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

    public MyUser getOwner() {
        return owner;
    }

    public void setOwner(MyUser owner) {
        this.owner = owner;
    }

    public GroupChat getGroupChat() {
        return groupChat;
    }

    public void setGroupChat(GroupChat groupChat) {
        this.groupChat = groupChat;
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

    public String getType(){
        if(urgent && important){
            return "Urgent/Important";
        }else if(urgent){
            return "Urgent";

        }else if(important){
            return "Important";
        }else{
            return "Not important or Urgent";
        }
    }

    public String getDueDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return dateFormat.format(endDate);
    }

    public String getStartDateString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return dateFormat.format(startDate);
    }

    public List<StudySession> getStudySessions() {
        return studySessions;
    }

    public void setStudySessions(List<StudySession> studySessions) {
        this.studySessions = studySessions;
    }

    public boolean isOverdue(){
        if(complete){
            return false;
        }
        return endDate.before(new Date());
    }
}
