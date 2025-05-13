package com.example.taskmanager.domain.Events;

import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.User.MyUser;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.util.Date;

@Entity
public class StudySession extends EventsBaseEntity {
    @ManyToOne
    private Assignment assignment;

    public StudySession( ) {
    }

    public StudySession(String title, String info, Date startDate, Date endDate, Date modified, boolean isOnline, String location, MyUser organiser) {
        super(title, info, startDate, endDate, modified, isOnline, location, organiser);
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }
}
