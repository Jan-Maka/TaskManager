package com.example.taskmanager.domain.User;

import jakarta.persistence.*;

@Entity
public class ProfilePic {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String type;
    @Lob
    @Column(name = "FileData", length = 10000000)
    private byte[] data;

    public ProfilePic() {
    }

    public ProfilePic(String name, String type, byte[] data) {
        this.name = name;
        this.type = type;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
