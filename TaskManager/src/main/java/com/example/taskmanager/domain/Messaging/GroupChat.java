package com.example.taskmanager.domain.Messaging;

import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.User.MyUser;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class GroupChat {
    @Id
    @GeneratedValue
    private Long id;

    private String groupName;

    @ManyToMany
    private List<MyUser> participants = new ArrayList<>();

    @OneToOne
    private GroupTask groupTask;

    @OneToOne
    private Assignment assignment;

    @OneToMany(mappedBy = "groupChat", cascade = CascadeType.REMOVE)
    private List<GroupMessage> messages = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    @UpdateTimestamp
    private Date latestMessageTimeStamp;

    @CreationTimestamp
    private Date creationDate;

    public GroupChat(String groupName, GroupTask groupTask) {
        this.groupName = groupName;
        this.groupTask = groupTask;
    }

    public GroupChat(String groupName, Assignment assignment) {
        this.groupName = groupName;
        this.assignment = assignment;
    }

    public GroupChat() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<MyUser> getParticipants() {
        return participants;
    }

    public void setParticipants(List<MyUser> participants) {
        this.participants = participants;
    }

    public GroupTask getGroupTask() {
        return groupTask;
    }

    public void setGroupTask(GroupTask groupTask) {
        this.groupTask = groupTask;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public List<GroupMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<GroupMessage> messages) {
        this.messages = messages;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void updateLatestTimeStamp(){
        latestMessageTimeStamp = new Date();
    }

    public Date getLatestMessageTimeStamp() {
        return latestMessageTimeStamp;
    }
}
