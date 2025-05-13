package com.example.taskmanager.domain.Task.DTO;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO used to display categorys and all the tasks they hold within for a API call
 */
public class CategoryDTO {

    private long id;
    private String name;
    private long user_id;

    private List<TaskDTO> tasks = new ArrayList<>();

    public CategoryDTO() {
    }

    public CategoryDTO(long id, String name, long user_id) {
        this.id = id;
        this.name = name;
        this.user_id = user_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }

    public List<TaskDTO> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDTO> tasks) {
        this.tasks = tasks;
    }
}
