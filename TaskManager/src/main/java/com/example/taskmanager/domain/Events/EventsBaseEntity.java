package com.example.taskmanager.domain.Events;

import com.example.taskmanager.domain.User.MyUser;
import jakarta.persistence.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@MappedSuperclass
public class EventsBaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @Column(length = 250)
    private String info;

    private Date startDate;

    private Date endDate;

    private Date modified;

    private boolean isOnline;

    private String location;

    @ManyToOne
    private MyUser organiser;

    public EventsBaseEntity() {
    }

    public EventsBaseEntity(String title, String info, Date startDate, Date endDate, Date modified, boolean isOnline, String location, MyUser organiser) {
        this.title = title;
        this.info = info;
        this.startDate = startDate;
        this.endDate = endDate;
        this.modified = modified;
        this.isOnline = isOnline;
        this.location = location;
        this.organiser = organiser;
    }

    public void setId(Long id) {
        this.id = id;
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

    public MyUser getOrganiser() {
        return organiser;
    }

    public void setOrganiser(MyUser organiser) {
        this.organiser = organiser;
    }

    public String getFormattedDateTimeEventString() {
        LocalDate date1 = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate date2 = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String date="";

        if(date1.isBefore(date2)) {
            date = _getFormattedStartDate() + " to " + _getFormattedEndDate();
        } else {
            date = _getFormattedStartDate() + " to " + _getEndTime();
        }
        return date;
    }

    private String _getFormattedStartDate() {
        String pattern = "E, MMM dd yyyy, HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(startDate);
        return date;
    }

    private String _getFormattedEndDate() {
        String pattern = "E, MMM dd yyyy, HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = simpleDateFormat.format(endDate);
        return date;
    }

    private String _getEndTime() {
        String pattern = "HH:mm";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String time = simpleDateFormat.format(endDate);
        return time;
    }
}
