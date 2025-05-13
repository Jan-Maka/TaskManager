package com.example.taskmanager.domain.Assignment;

import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Task.TaskBaseEntity;
import com.example.taskmanager.domain.User.MyUser;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class AssignmentTask extends TaskBaseEntity {

    @ManyToOne
    @JoinColumn(name = "assignment")
    private Assignment assignment;

    @ManyToMany
    private List<MyUser> users = new ArrayList<>();

    @ManyToOne
    private MyUser owner;

    public AssignmentTask(MyUser owner,String title, boolean important, boolean urgent, short workload, short mood, String description, Date startDate, Date endDate, int progress, Date modified, boolean complete, boolean inProgress, Assignment assignment) {
        super(title, important, urgent, workload, mood, description, startDate, endDate, progress, modified, complete, inProgress);
        this.owner = owner;
        this.assignment = assignment;
    }

    public AssignmentTask() {
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public List<MyUser> getUsers() {
        return users;
    }

    public void setUsers(List<MyUser> users) {
        this.users = users;
    }

    public MyUser getOwner() {
        return owner;
    }

    public void setOwner(MyUser owner) {
        this.owner = owner;
    }
}
