package com.example.classic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    public String name;
    public String email;
    public String password;
    public String role;
    public String status;
}
