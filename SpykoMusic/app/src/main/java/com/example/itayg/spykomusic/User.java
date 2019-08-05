package com.example.itayg.spykomusic;

public class User {
    public String fullName;
    public String nickname;
    public String email;

    public User(){}

    public User(String firstName, String lastName, String email) {
        this.fullName = firstName;
        this.nickname = lastName;
        this.email = email;
    }
}
