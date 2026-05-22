package com.example.classic;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class MainRestController {



    @PostMapping("/user/add")
    public ResponseEntity<String> addUser(@RequestParam String name, @RequestParam String email, @RequestParam String password,
                                          @RequestParam String role, @RequestParam String status) {
        String data = name + "|" + email + "|" + password + "|" + role + "|" + status;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("classpath:data/user.txt", true))) {
            writer.write(data);
            writer.newLine();
        } catch (IOException ex) {
            System.out.println(ex);
        }

        return ResponseEntity.ok("User saved");
    }
}
