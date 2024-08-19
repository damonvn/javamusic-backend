package com.example.javamusic.controller;

import java.util.List;

import com.example.javamusic.model.User;
import com.example.javamusic.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
     @Autowired
    private UserRepository userRepository;

    // API để tạo mới User
    @PostMapping
    public User createUser(@RequestBody User user) {
        
        // User savedUser = userRepository.save(user);
        // return ResponseEntity.ok(savedUser);
        return userRepository.save(user);
    }

    // API để lấy tất cả User
    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // API để lấy User theo ID
    @GetMapping("/{id}")
    public User getUserById(@PathVariable String id) {
        return userRepository.findById(id).orElse(null);
    }

    // API để cập nhật User theo ID
    @PutMapping("/{id}")
    public User updateUser(@PathVariable String id, @RequestBody User userDetails) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setPassword(userDetails.getPassword());
            user.setAge(userDetails.getAge());
            user.setGender(userDetails.getGender());
            user.setAddress(userDetails.getAddress());
            user.setRole(userDetails.getRole());
            return userRepository.save(user);
        } else {
            return null;
        }
    }

    // API để xóa User theo ID
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
    }
}
