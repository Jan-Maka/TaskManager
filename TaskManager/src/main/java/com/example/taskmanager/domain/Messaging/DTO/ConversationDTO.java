package com.example.taskmanager.domain.Messaging.DTO;

import com.example.taskmanager.domain.Messaging.Conversation;
import com.example.taskmanager.domain.User.DTO.UserDTO;

import java.util.Date;
import java.util.List;

/**
 * DTO used for conversations to display details from API calls
 */
public class ConversationDTO {
    private Long id;

    private List<UserDTO> participants;

    private List<ConversationMessageDTO> messages;

    private Date latestMessageTimeStamp;

    public ConversationDTO(Long id, List<UserDTO> participants, List<ConversationMessageDTO> messages, Date latestMessageTimeStamp) {
        this.id = id;
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

    public List<UserDTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<UserDTO> participants) {
        this.participants = participants;
    }

    public List<ConversationMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<ConversationMessageDTO> messages) {
        this.messages = messages;
    }

    public Date getLatestMessageTimeStamp() {
        return latestMessageTimeStamp;
    }

    public void setLatestMessageTimeStamp(Date latestMessageTimeStamp) {
        this.latestMessageTimeStamp = latestMessageTimeStamp;
    }
}
