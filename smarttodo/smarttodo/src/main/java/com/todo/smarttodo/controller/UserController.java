package com.todo.smarttodo.controller;

import com.todo.smarttodo.dto.UserRequestDTO;
import com.todo.smarttodo.dto.UserResponseDTO;
import com.todo.smarttodo.entity.User;
import com.todo.smarttodo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // Constructor Injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Register new user
    //@PostMapping("/register")
    //public User registerUser(@RequestBody User user) {
        //return userService.registerUser(user);
    //}

    @PostMapping("/register")
    public UserResponseDTO registerUser(@RequestBody @Valid UserRequestDTO dto) {
        return userService.registerUser(dto);
    }

    // Get user by id
    //@GetMapping("/{id}")
    //public User getUserById(@PathVariable Long id) {
        //return userService.getUserById(id)
                //.orElseThrow(() -> new RuntimeException("User not found"));
    //}

    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);  // Already returns UserResponseDTO
    }


}
