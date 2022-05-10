package com.example.mychatapp.Model;

public class Users {
    String username;
    String imageURL;
    String id;
    String status;
    String backgroundURL;

    public Users(String username, String imageURL, String id, String status, String backgroundURL) {
        this.username = username;
        this.imageURL = imageURL;
        this.id = id;
        this.status = status;
        this.backgroundURL = backgroundURL;
    }

    public Users() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBackgroundURL() {
        return backgroundURL;
    }

    public void setBackgroundURL(String backgroundURL) {
        this.backgroundURL = backgroundURL;
    }
}
