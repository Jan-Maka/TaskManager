package com.example.taskmanager.domain.Task;

import com.example.taskmanager.domain.Messaging.GroupChat;
import com.example.taskmanager.domain.User.MyUser;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class GroupTask extends TaskBaseEntity{
    @ManyToOne
    private MyUser owner;

    @ManyToMany
    private List<MyUser> users = new ArrayList<>();

    @OneToOne(mappedBy = "groupTask", cascade = CascadeType.ALL)
    private GroupChat groupChat;

    public GroupTask() {
    }

    public GroupTask(String title, boolean important, boolean urgent, short workload, short mood, String description, Date startDate, Date endDate, int progress, Date modified, boolean complete, boolean inProgress, MyUser owner, List<MyUser> users) {
        super(title, important, urgent, workload, mood, description, startDate, endDate, progress, modified, complete, inProgress);
        this.owner = owner;
        this.users = users;
    }


    public MyUser getOwner() {
        return owner;
    }

    public void setOwner(MyUser owner) {
        this.owner = owner;
    }

    public List<MyUser> getUsers() {
        return users;
    }

    public void setUsers(List<MyUser> users) {
        this.users = users;
    }

    public GroupChat getGroupChat() {
        return groupChat;
    }

    public void setGroupChat(GroupChat groupChat) {
        this.groupChat = groupChat;
    }
}
