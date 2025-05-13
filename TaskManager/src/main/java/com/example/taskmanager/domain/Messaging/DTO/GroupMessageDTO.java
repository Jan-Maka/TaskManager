package com.example.taskmanager.domain.Messaging.DTO;

import com.example.taskmanager.domain.File.DTO.FileAttachmentDTO;

import java.util.Date;
/**
 * DTO used for messages details sent to controller or received from STOMP endpoint
 */
public class GroupMessageDTO {

    private String content;

    private Long sender;

    private String senderUsername;

    private Date sent;

    private Long groupChat;

    private FileAttachmentDTO file;

    public GroupMessageDTO() {
    }

    public GroupMessageDTO(String content, Long sender,String senderUsername,Date sent,  Long groupChat) {
        this.content = content;
        this.sender = sender;
        this.senderUsername = senderUsername;
        this.sent = sent;
        this.groupChat = groupChat;
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

    public Long getGroupChat() {
        return groupChat;
    }

    public void setGroupChat(Long groupChat) {
        this.groupChat = groupChat;
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
