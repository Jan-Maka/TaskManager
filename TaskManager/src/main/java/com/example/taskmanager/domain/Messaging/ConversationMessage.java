package com.example.taskmanager.domain.Messaging;

import com.example.taskmanager.domain.Task.Task;
import com.example.taskmanager.domain.User.MyUser;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class ConversationMessage extends MessageBaseEntity{

    @ManyToOne
    private Conversation conversation;

    @ManyToOne
    private MyUser sender;

    @ManyToOne
    private Task task;


    public ConversationMessage(String content, Conversation conversation, MyUser sender) {
        super(content);
        this.conversation = conversation;
        this.sender = sender;
    }

    public ConversationMessage() {}

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public MyUser getSender() {
        return sender;
    }

    public void setSender(MyUser user) {
        this.sender = user;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
