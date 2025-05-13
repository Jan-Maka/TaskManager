package com.example.taskmanager.domain.Messaging;

import com.example.taskmanager.domain.User.MyUser;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class GroupMessage extends MessageBaseEntity {

    @ManyToOne
    private GroupChat groupChat;

    @ManyToOne
    private MyUser sender;

    public GroupMessage(String content, GroupChat groupChat, MyUser sender) {
        super(content);
        this.groupChat = groupChat;
        this.sender = sender;
    }

    public GroupMessage() {}

    public GroupChat getGroupChat() {
        return groupChat;
    }

    public void setGroupChat(GroupChat groupChat) {
        this.groupChat = groupChat;
    }

    public MyUser getSender() {
        return sender;
    }

    public void setSender(MyUser user) {
        this.sender = user;
    }
}
