package com.example.javamusic.model;

public class LoginModel {
    private String email;
    private String password;

    public LoginModel() {
    }

    // Constructor có tham số
    public LoginModel(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getter cho email
    public String getEmail() {
        return email;
    }

    // Setter cho email
    public void setEmail(String email) {
        this.email = email;
    }

    // Getter cho password
    public String getPassword() {
        return password;
    }

    // Setter cho password
    public void setPassword(String password) {
        this.password = password;
    }
}
