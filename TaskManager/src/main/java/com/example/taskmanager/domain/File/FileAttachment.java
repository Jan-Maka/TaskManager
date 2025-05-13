package com.example.taskmanager.domain.File;

import com.example.taskmanager.domain.Assignment.Assignment;
import com.example.taskmanager.domain.Assignment.AssignmentTask;
import com.example.taskmanager.domain.Task.GroupTask;
import com.example.taskmanager.domain.Task.Task;
import jakarta.persistence.*;

@Entity
public class FileAttachment {
    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private String fileType;

    private long size;

    @Lob
    @Column(length = 100000)
    private byte[] data;


    @ManyToOne
    private Task task;

    @ManyToOne
    private GroupTask groupTask;

    @ManyToOne
    private Assignment assignment;

    @ManyToOne
    private AssignmentTask assignmentTask;

    public FileAttachment() {
    }

    public FileAttachment(String title, String fileType, long size,byte[] data) {
        this.title = title;
        this.fileType = fileType;
        this.size = size;
        this.data = data;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getId() {
        return id;
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

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public GroupTask getGroupTask() {
        return groupTask;
    }

    public void setGroupTask(GroupTask groupTask) {
        this.groupTask = groupTask;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public AssignmentTask getAssignmentTask() {
        return assignmentTask;
    }

    public void setAssignmentTask(AssignmentTask assignmentTask) {
        this.assignmentTask = assignmentTask;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
