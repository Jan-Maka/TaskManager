package com.example.taskmanager.domain.Messaging;

import com.example.taskmanager.domain.User.MyUser;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Conversation {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<MyUser> participants = new ArrayList<>();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    private List<ConversationMessage> messages = new ArrayList<>();

    @UpdateTimestamp
    private Date latestMessageTimeStamp;

    @CreationTimestamp
    private Date creationDate;

    public Conversation() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public List<ConversationMessage> getMessages(){
        return messages;
    }

    public void setMessages(List<ConversationMessage> messages) {
        this.messages = messages;
    }

    public List<MyUser> getParticipants() {
        return participants;
    }

    public void setParticipants(List<MyUser> participants) {
        this.participants = participants;
    }

    public Date getCreated() {
        return creationDate;
    }

    public void updateLatestTimeStamp(){
        latestMessageTimeStamp = new Date();
    }

    public Date getLatestMessageTimeStamp() {
        return latestMessageTimeStamp;
    }
}
