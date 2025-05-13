package com.example.taskmanager.domain.Messaging.DTO;

import com.example.taskmanager.domain.User.DTO.UserDTO;

import java.util.Date;
import java.util.List;
/**
 * DTO used for group chats to display details from API calls
 */
public class GroupChatDTO {

    private Long id;

    private String groupName;

    private List<UserDTO> participants;

    private List<GroupMessageDTO> messages;

    private Long assignment;

    private Long groupTask;

    private Date latestMessageTimeStamp;

    public GroupChatDTO(Long id, String groupName, List<UserDTO> participants, List<GroupMessageDTO> messages, Date latestMessageTimeStamp) {
        this.id = id;
        this.groupName = groupName;
        this.participants = participants;
        this.messages = messages;
        this.latestMessageTimeStamp = latestMessageTimeStamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<UserDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<UserDTO> participants) {
        this.participants = participants;
    }

    public List<GroupMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<GroupMessageDTO> messages) {
        this.messages = messages;
    }

    public Long getAssignment() {
        return assignment;
    }

    public void setAssignment(Long assignment) {
        this.assignment = assignment;
    }

    public Long getGroupTask() {
        return groupTask;
    }

    public void setGroupTask(Long groupTask) {
        this.groupTask = groupTask;
    }

    public Date getLatestMessageTimeStamp() {
        return latestMessageTimeStamp;
    }

    public void setLatestMessageTimeStamp(Date latestMessageTimeStamp) {
        this.latestMessageTimeStamp = latestMessageTimeStamp;
    }
}
