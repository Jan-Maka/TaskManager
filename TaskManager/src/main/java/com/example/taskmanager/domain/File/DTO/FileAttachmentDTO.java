package com.example.taskmanager.domain.File.DTO;

/**
 * DTO used for files to get details from API calls
 */
public class FileAttachmentDTO {

    private long id;
    private String title;
    private String fileType;
    private long size;


    public FileAttachmentDTO() {
    }

    public FileAttachmentDTO(long id, String title, String fileType, long size) {
        this.id = id;
        this.title = title;
        this.fileType = fileType;
        this.size = size;
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

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
