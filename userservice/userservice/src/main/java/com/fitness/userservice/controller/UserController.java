package com.fitness.userservice.controller;

import com.fitness.userservice.dto.RegisterUser;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userid}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable String userid)
    {
        return ResponseEntity.ok(userService.getUserProfile(userid));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterUser user)
    {
        return ResponseEntity.ok(userService.register(user));
    }
}
