package com.example.taskmanager.domain.Messaging;

import com.example.taskmanager.domain.File.FileAttachment;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@MappedSuperclass
public class MessageBaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String content;

    @CreationTimestamp
    private Date dateSent;

    @OneToOne(cascade = CascadeType.REMOVE)
    private FileAttachment fileAttachment;

    public MessageBaseEntity() {
    }

    public MessageBaseEntity(String content) {
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public FileAttachment getFileAttachment() {
        return fileAttachment;
    }

    public void setFileAttachment(FileAttachment fileAttachment) {
        this.fileAttachment = fileAttachment;
    }
}
