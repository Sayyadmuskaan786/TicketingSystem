package com.example.formbackend.controller;

import com.example.formbackend.service.UserService;
import com.example.formbackend.model.EmailVerificationToken;
import com.example.formbackend.model.User;
import com.example.formbackend.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

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

    // Other authentication endpoints...

}
