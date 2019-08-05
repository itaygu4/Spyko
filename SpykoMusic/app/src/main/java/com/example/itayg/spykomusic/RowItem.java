package com.example.itayg.spykomusic;


public class RowItem {

    private String firebaseUserUid;
    private String fullName, nickname, actuallyFollowing;

    public RowItem(String firebaseUserUid, String name, String nickname, String actuallyFollowing) {
        this.firebaseUserUid = firebaseUserUid;
        this.fullName = name;
        this.nickname = nickname;
        this.actuallyFollowing = actuallyFollowing;

    }

    public String getFirebaseUserUid() {
        return firebaseUserUid;
    }

    public void setFirebaseUser(String firebaseUserUid) {
        this.firebaseUserUid = firebaseUserUid;
    }

    public String getName() {
        return fullName;
    }

    public void setName(String name) {
        this.fullName = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getActuallyFollowing() {
        return actuallyFollowing;
    }
}
