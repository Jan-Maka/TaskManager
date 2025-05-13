package com.example.taskmanager.domain.Task;

import com.example.taskmanager.domain.File.FileAttachment;
import com.example.taskmanager.domain.User.MyUser;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Entity
public class Task extends TaskBaseEntity{

    @ManyToOne
    private MyUser user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private boolean archive = false;

    private LocalDate unarchiveTimeStamp;


    public Task() {
    }

    public Task(MyUser user, String title, boolean important, boolean urgent, short workload, short mood, String description, Category category, Date startDate, Date endDate, int progress, Date modified, boolean complete, boolean inProgress) {
        super(title,important,urgent,workload,mood,description,startDate,endDate,progress,modified,complete,inProgress);
        this.user = user;
        this.category = category;
    }

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }


    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public LocalDate getUnarchiveTimeStamp() {
        return unarchiveTimeStamp;
    }

    public void setUnarchiveTimeStamp(LocalDate unarchiveTimeStamp) {
        this.unarchiveTimeStamp = unarchiveTimeStamp;
    }
}
