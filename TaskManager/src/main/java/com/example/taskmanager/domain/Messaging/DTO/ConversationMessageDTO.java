package com.example.taskmanager.domain.Messaging.DTO;

import com.example.taskmanager.domain.File.DTO.FileAttachmentDTO;

import java.util.Date;

/**
 * DTO used for messages details sent to controller or received from STOMP endpoint
 */
public class ConversationMessageDTO {

    private String content;
    private Long sender;

    private String senderUsername;

    private Date sent;
    private Long recipient;
    private Long conversation;

    private Long task = Long.valueOf(0);

    private FileAttachmentDTO file;

    public ConversationMessageDTO() {
    }

    public ConversationMessageDTO(String content, Long sender, String senderUsername, Date sent ,Long recipient, Long conversation) {
        this.content = content;
        this.sender = sender;
        this.senderUsername = senderUsername;
        this.sent = sent;
        this.recipient = recipient;
        this.conversation = conversation;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSender() {
        return sender;
    }

    public void setSender(Long sender) {
        this.sender = sender;
    }

    public Long getRecipient() {
        return recipient;
    }

    public void setRecipient(Long recipient) {
        this.recipient = recipient;
    }

    public Long getConversation() {
        return conversation;
    }

    public void setConversation(Long conversation) {
        this.conversation = conversation;
    }

    public Long getTask() {
        return task;
    }

    public void setTask(Long task) {
        this.task = task;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }

    public FileAttachmentDTO getFile() {
        return file;
    }

    public void setFile(FileAttachmentDTO file) {
        this.file = file;
    }
}
