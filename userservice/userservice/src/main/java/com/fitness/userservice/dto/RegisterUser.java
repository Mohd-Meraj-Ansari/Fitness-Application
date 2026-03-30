package com.fitness.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegisterUser {
    private String firstName;
    private String lastName;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "password must be atleast 6 characters long")
    private String password;

    @NotBlank(message = "email is required")
    @Email(message = "invalid email format")
    private String email;



}

