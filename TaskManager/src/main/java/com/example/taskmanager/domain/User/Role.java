package com.example.taskmanager.domain.User;

import com.example.taskmanager.domain.User.MyUser;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
public class Role {
    @Id
    private String name;

    @ManyToMany(mappedBy = "roles")
    private List<MyUser> users = new ArrayList<>();

    @ManyToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn
    private List<Privilege> privileges = new ArrayList<>();


    public Role(String name) {
        this.name = name;
    }

    public Role() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MyUser> getUsers() {
        return users;
    }

    public void setUsers(List<MyUser> users) {
        this.users = users;
    }

    public List<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges) {
        this.privileges = privileges;
    }
}
