package com.example.taskmanager.domain.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class AccountSettings {

    @Id
    @GeneratedValue
    private Long id;

    private PrivacyAccess displayEmail = PrivacyAccess.ALL;
    private PrivacyAccess displayName = PrivacyAccess.ALL;
    private PrivacyAccess displayLocation = PrivacyAccess.ALL;
    private PrivacyAccess displayTasks = PrivacyAccess.ALL;
    private PrivacyAccess displayNumber = PrivacyAccess.NONE;
    private boolean accountPrivate = false;

    private boolean emailTaskReminders = false;
    private boolean emailAssignmentReminders = false;
    private boolean emailStudySessionReminders = false;


    private boolean sendAssignmentNotifications = false;

    private boolean sendTaskNotifications = false;


    private boolean sendStudySessionNotifications = false;

    public PrivacyAccess getDisplayEmail() {
        return displayEmail;
    }

    public void setDisplayEmail(PrivacyAccess displayEmail) {
        this.displayEmail = displayEmail;
    }

    public PrivacyAccess getDisplayName() {
        return displayName;
    }

    public void setDisplayName(PrivacyAccess displayName) {
        this.displayName = displayName;
    }

    public PrivacyAccess getDisplayLocation() {
        return displayLocation;
    }

    public void setDisplayLocation(PrivacyAccess displayLocation) {
        this.displayLocation = displayLocation;
    }

    public PrivacyAccess getDisplayTasks() {
        return displayTasks;
    }

    public void setDisplayTasks(PrivacyAccess displayTasks) {
        this.displayTasks = displayTasks;
    }

    public PrivacyAccess getDisplayNumber() {
        return displayNumber;
    }

    public void setDisplayNumber(PrivacyAccess displayNumber) {
        this.displayNumber = displayNumber;
    }

    public boolean isAccountPrivate() {
        return accountPrivate;
    }

    public void setAccountPrivate(boolean accountPrivate) {
        this.accountPrivate = accountPrivate;
    }

    public boolean isEmailTaskReminders() {
        return emailTaskReminders;
    }

    public void setEmailTaskReminders(boolean emailTaskReminders) {
        this.emailTaskReminders = emailTaskReminders;
    }

    public boolean isEmailAssignmentReminders() {
        return emailAssignmentReminders;
    }

    public void setEmailAssignmentReminders(boolean emailAssignmentReminders) {
        this.emailAssignmentReminders = emailAssignmentReminders;
    }

    public boolean isEmailStudySessionReminders() {
        return emailStudySessionReminders;
    }

    public void setEmailStudySessionReminders(boolean emailStudySessionReminders) {
        this.emailStudySessionReminders = emailStudySessionReminders;
    }

    public boolean isSendAssignmentNotifications() {
        return sendAssignmentNotifications;
    }

    public void setSendAssignmentNotifications(boolean sendAssignmentNotifications) {
        this.sendAssignmentNotifications = sendAssignmentNotifications;
    }

    public boolean isSendTaskNotifications() {
        return sendTaskNotifications;
    }

    public void setSendTaskNotifications(boolean sendTaskNotifications) {
        this.sendTaskNotifications = sendTaskNotifications;
    }

//    public boolean isSendChatNotifications() {
//        return sendChatNotifications;
//    }
//
//    public void setSendChatNotifications(boolean sendChatNotifications) {
//        this.sendChatNotifications = sendChatNotifications;
//    }

    public boolean isSendStudySessionNotifications() {
        return sendStudySessionNotifications;
    }

    public void setSendStudySessionNotifications(boolean sendStudySessionNotifications) {
        this.sendStudySessionNotifications = sendStudySessionNotifications;
    }

    public String getDisplayEmailString(){
        switch (displayEmail){
            case FRIEND:
                return "friend";
            case NONE:
                return "none";
            default:
                return "all";
        }
    }
    public String getDisplayNameString(){
        switch (displayName){
            case FRIEND:
                return "friend";
            case NONE:
                return "none";
            default:
                return "all";
        }
    }
    public String getDisplayLocationString(){
        switch (displayLocation){
            case FRIEND:
                return "friend";
            case NONE:
                return "none";
            default:
                return "all";
        }
    }

    public String getDisplayNumberString(){
        switch (displayNumber){
            case FRIEND:
                return "friend";
            case NONE:
                return "none";
            default:
                return "all";
        }
    }

    public String getDisplayTasksString(){
        switch (displayTasks){
            case FRIEND:
                return "friend";
            case NONE:
                return "none";
            default:
                return "all";
        }
    }
}
