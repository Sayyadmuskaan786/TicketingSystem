package com.example.formbackend.controller;

import com.example.formbackend.service.UserService;
import com.example.formbackend.model.EmailVerificationToken;
import com.example.formbackend.model.User;
import com.example.formbackend.payload.LoginRequest;
import com.example.formbackend.payload.LoginResponse;
import com.example.formbackend.repository.EmailVerificationTokenRepository;
import com.example.formbackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            return ResponseEntity.badRequest().body("Invalid verification token");
        }
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Verification token expired");
        }
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userService.saveUser(user);
        tokenRepository.delete(verificationToken);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("Login attempt with email: " + loginRequest.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            User user = userService.getUserByUsername(loginRequest.getEmail());
            if (user == null) {
                System.out.println("User not found for email: " + loginRequest.getEmail());
                return ResponseEntity.badRequest().body("Invalid email or password");
            }
            // Removed email verification check to allow login without verification

            String token = jwtUtil.generateToken(user.getEmail());

            LoginResponse loginResponse = new LoginResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name(),
                    token
            );

            return ResponseEntity.ok(loginResponse);
        } catch (AuthenticationException e) {
            System.out.println("Authentication failed for email: " + loginRequest.getEmail() + " - " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid email or password");
        }
    }
}
