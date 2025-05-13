package com.example.taskmanager.domain.User;

import jakarta.persistence.*;

@Entity
public class FriendRequest {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private MyUser sender;

    @ManyToOne
    private MyUser recipient;

    public FriendRequest() {
    }

    public FriendRequest(MyUser sender, MyUser recipient) {
        this.sender = sender;
        this.recipient = recipient;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public MyUser getSender() {
        return sender;
    }

    public void setSender(MyUser sender) {
        this.sender = sender;
    }

    public MyUser getRecipient() {
        return recipient;
    }

    public void setRecipient(MyUser recipient) {
        this.recipient = recipient;
    }
}
