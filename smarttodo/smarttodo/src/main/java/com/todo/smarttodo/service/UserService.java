package com.todo.smarttodo.service;

import com.todo.smarttodo.entity.User;
import com.todo.smarttodo.exception.ResourceNotFoundException;
import com.todo.smarttodo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;
import com.todo.smarttodo.dto.UserRequestDTO;
import com.todo.smarttodo.dto.UserResponseDTO;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder; // for encrypting passwords

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // initialize encoder
    }

    // Register a new user
    //public User registerUser(User user) {
        // Encrypt the password before saving
        //user.setPassword(passwordEncoder.encode(user.getPassword()));
        //return userRepository.save(user);
    //}
    public UserResponseDTO registerUser(UserRequestDTO dto) {
        // Check if email already exists
        if (emailExists(dto.getEmail())) {
            throw new ResourceNotFoundException("Email already registered");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userRepository.save(user);

        return mapToResponseDTO(savedUser);
    }


    // Get user by ID
    //public Optional<User> getUserById(Long id) {
        //return userRepository.findById(id);
    //}
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponseDTO(user);
    }


    // Get user by email (used for login later)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Check if email already exists
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    private UserResponseDTO mapToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }

}
