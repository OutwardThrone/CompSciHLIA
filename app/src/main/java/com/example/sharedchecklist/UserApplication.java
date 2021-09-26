package com.example.sharedchecklist;

import android.app.Application;

public class UserApplication extends Application {

    private User loggedInUser;

    public void setUser(User user) {
        this.loggedInUser = user;
    }

    public User getUser() {
        return loggedInUser;
    }
}
