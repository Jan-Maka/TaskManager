package com.example.taskmanager.domain.User;

import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Assignment.AssignmentTask;
import com.example.taskmanager.domain.Events.StudySession;
import com.example.taskmanager.domain.Messaging.GroupChat;
import com.example.taskmanager.domain.Messaging.Conversation;
import com.example.taskmanager.domain.Task.Category;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.Task.Task;
import groovy.transform.builder.Builder;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Builder
@Entity
public class MyUser {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email address is invalid", regexp = ".+[@].+[\\.].+")
    private String email;

    private LocalDateTime subscriptionStart;
    private String firstName;
    private String surname;
    private String password;

    private String username;

    private String location;

    private String phoneNumber;

    private String Country;

    @Column(length = 150)
    private String bio;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn
    private List<Role> roles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> categories = new ArrayList<>();

    @ManyToMany(mappedBy = "users")
    private List<GroupTask> groupTasks = new ArrayList<>();

    @ManyToMany
    private List<MyUser> friends = new ArrayList<>();

    @ManyToMany(mappedBy = "users")
    private List<Assignment> assignments = new ArrayList<>();

    @ManyToMany
    private List<StudySession> studySessions = new ArrayList<>();

    @ManyToMany(mappedBy = "users")
    private List<AssignmentTask> assignmentTasks = new ArrayList<>();

    private short moodRating = 0;

    @ElementCollection
    private List<Short> moodRatingsPast7Days = new ArrayList<>(7);
    @OneToOne
    private ProfilePic profilePic = null;

    @OneToOne
    private AccountSettings accountSettings;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    private List<FriendRequest> receivedFriendRequests = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<FriendRequest> sentFriendRequests = new ArrayList<>();

    @ManyToMany(mappedBy = "participants")
    private List<Conversation> conversations = new ArrayList<>();

    @ManyToMany(mappedBy = "participants")
    private List<GroupChat> groupChats = new ArrayList<>();

    public MyUser() {
    }

    public MyUser(String email, String firstName, String surname, String password, String username) {
        this.email = email;
        this.firstName = firstName;
        this.surname = surname;
        this.password = password;
        this.username = username;
    }

    private LocalDate lastLoginDate;

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Long getId() {
        return id;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<MyUser> getFriends() {
        return friends;
    }

    public void setFriends(List<MyUser> friends) {
        this.friends = friends;
    }

    public List<GroupTask> getGroupTasks() {
        return groupTasks;
    }

    public void setGroupTasks(List<GroupTask> groupTasks) {
        this.groupTasks = groupTasks;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    public List<AssignmentTask> getAssignmentTasks() {
        return assignmentTasks;
    }

    public void setAssignmentTasks(List<AssignmentTask> assignmentTasks) {
        this.assignmentTasks = assignmentTasks;
    }

    public List<StudySession> getStudySessions() {
        return studySessions;
    }

    public void setStudySessions(List<StudySession> studySessions) {
        this.studySessions = studySessions;
    }

    public short getMoodRating() {
        return moodRating;
    }

    public void setMoodRating(short moodRating) {
        this.moodRating = moodRating;
    }

    public List<Short> getMoodRatingsPast7Days() {
        return moodRatingsPast7Days;
    }

    public void setMoodRatingsPast7Days(List<Short> moodRatingsPast7Days) {
        this.moodRatingsPast7Days = moodRatingsPast7Days;
    }

    public ProfilePic getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(ProfilePic profilePic) {
        this.profilePic = profilePic;
    }

    public AccountSettings getAccountSettings() {
        return accountSettings;
    }

    public void setAccountSettings(AccountSettings accountSettings) {
        this.accountSettings = accountSettings;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFullName(){
        return this.firstName+" "+this.surname;
    }

    public List<FriendRequest> getReceivedFriendRequests() {
        return receivedFriendRequests;
    }

    public void setReceivedFriendRequests(List<FriendRequest> receivedFriendRequests) {
        this.receivedFriendRequests = receivedFriendRequests;
    }

    public List<FriendRequest> getSentFriendRequests() {
        return sentFriendRequests;
    }

    public void setSentFriendRequests(List<FriendRequest> sentFriendRequests) {
        this.sentFriendRequests = sentFriendRequests;
    }

    public String getBase64ProfilePic() {
        return (profilePic != null) ? "data:image/png;base64," + Base64.getEncoder().encodeToString(profilePic.getData()) : null;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public List<GroupChat> getGroupChats() {
        return groupChats;
    }

    public void setGroupChats(List<GroupChat> groupChats) {
        this.groupChats = groupChats;
    }

    public boolean isUserFriend (MyUser user){
        return friends.contains(user);
    }

    public LocalDate getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(LocalDate lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public LocalDateTime getSubscriptionStart() {
        return subscriptionStart;
    }

    public void setSubscriptionStart(LocalDateTime subscriptionStart) {
        this.subscriptionStart = subscriptionStart;
    }

    public boolean hasRole(String roleName){
        return roles.stream().anyMatch((role -> role.getName().equals(roleName)));
    }

    public String getUserRolesIntoString(){
        StringBuilder rolesString = new StringBuilder();
        roles.forEach(role -> rolesString.append(role.getName()+","));
        String rolesConcatenated = rolesString.toString();
        return rolesConcatenated;
    }
}
