package com.example.taskmanager.domain.Events.DTO;

import com.example.taskmanager.domain.User.DTO.UserDTO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * DTO used for study sessions to display details from API calls
 */
public class StudySessionDTO {

    private long id;

    private String title;

    private String info;

    private Date startDate;

    private Date endDate;

    private Date modified;

    private boolean isOnline;

    private String location;

    private long organiser;

    private String user;

    private long assignment;

    private String assignmentName;

    public StudySessionDTO(long id, String title, String info, Date startDate, Date endDate, Date modified, boolean isOnline, String location, long organiser, String user) {
        this.id = id;
        this.title = title;
        this.info = info;
        this.startDate = startDate;
        this.endDate = endDate;
        this.modified = modified;
        this.isOnline = isOnline;
        this.location = location;
        this.organiser = organiser;
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
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

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getOrganiser() {
        return organiser;
    }

    public void setOrganiser(long organiser) {
        this.organiser = organiser;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getAssignment() {
        return assignment;
    }

    public void setAssignment(long assignment) {
        this.assignment = assignment;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public void setAssignmentName(String assignmentName) {
        this.assignmentName = assignmentName;
    }
}
