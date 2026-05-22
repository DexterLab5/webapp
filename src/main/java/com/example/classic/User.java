package com.example.classic;

import lombok.Data;

@Data
public class User {
    public String name;
    public String email;
    public String password;
    public String role;
    public String status;
    public String joinedDate;

    public User(String name, String email, String password, String role, String status, String joinedDate) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.joinedDate = joinedDate;
    }

    public User() {

    }
}
