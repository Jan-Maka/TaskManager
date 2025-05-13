package com.example.taskmanager.domain.User;

import jakarta.persistence.*;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

@Entity
public class PasswordResetToken {
    public static final int EXPIRATION = 48;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne
    private MyUser user;

    private Date expiryDate;

    public PasswordResetToken() {
    }

    public PasswordResetToken(MyUser user) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        setExpiryDate();
    }

    public long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate() {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, EXPIRATION);
        expiryDate = new Date(cal.getTime().getTime());
    }
}
