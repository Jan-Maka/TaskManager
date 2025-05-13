package com.example.taskmanager.domain.User.DTO;

/**
 * Displays some details about a user which is mainly used for other entities that contains user for API calls
 */
public class UserDTO {

    private Long id;

    private String username;

    private String bio;

    private String location;

    private String profilePic;

    public UserDTO(){
    }

    public UserDTO(Long id, String username, String bio, String location, String profilePic) {
        this.id = id;
        this.username = username;
        this.bio = bio;
        this.location = location;
        this.profilePic = profilePic;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }
}
