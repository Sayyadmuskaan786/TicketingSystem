package com.example.formbackend.controller;

import com.example.formbackend.service.UserService;
import com.example.formbackend.model.User;
import com.example.formbackend.payload.LoginRequest;
import com.example.formbackend.payload.LoginResponse;
import com.example.formbackend.payload.OtpRequest;
import com.example.formbackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));

            User user = userService.getUserByUsername(loginRequest.getEmail());
            if (user == null) {
                return ResponseEntity.badRequest().body("Invalid email or password");
            }
            // Optionally, check if user is verified here

            String token = jwtUtil.generateToken(user.getEmail());

            LoginResponse loginResponse = new LoginResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name(),
                    token);

            return ResponseEntity.ok(loginResponse);
        } catch (AuthenticationException e) {
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
    }

    @PostMapping("/register-otp")
    public ResponseEntity<String> registerWithOtp(@RequestBody User user) {
        String response = userService.registerUser(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest request) {
        String result = userService.verifyOtp(request.getEmail(), request.getOtp());
        if ("Verification successful.".equals(result)) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
