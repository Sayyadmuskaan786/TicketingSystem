package com.example.formbackend.controller;

import com.example.formbackend.dto.UserDTO;
import com.example.formbackend.model.User;
import com.example.formbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/agents")
    public ResponseEntity<java.util.List<UserDTO>> getAllAgents() {
        java.util.List<UserDTO> agents = userService.getAllAgents().stream()
            .map(UserDTO::new)
            .toList();
        return ResponseEntity.ok(agents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userService.getUserById(id);
        if (userOptional.isPresent()) {
            UserDTO userDTO = new UserDTO(userOptional.get());
            return ResponseEntity.ok(userDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<java.util.List<UserDTO>> getAllUsers() {
        java.util.List<UserDTO> users = userService.getAllUsers().stream()
            .map(UserDTO::new)
            .toList();
        return ResponseEntity.ok(users);
    }
}
