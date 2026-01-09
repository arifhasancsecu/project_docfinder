package com.titukumar.doctor.model;

public class User {
    private String username;
    private String phoneNo;
    private String age;
    private String gender;
    private String created_at;
    private String updated_at;
    private String userProfile;
    private Boolean verified;
    public User() {
    }

    public User(String username, String age, String gender, String phoneNo, String created_at, String updated_at, String userProfile) {
        this.username = username;
        this.phoneNo = phoneNo;
        this.age = age;
        this.gender = gender;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.userProfile = userProfile;
        this.verified = false;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
}
