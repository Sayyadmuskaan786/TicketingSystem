package com.example.formbackend.service;

import com.example.formbackend.model.User;
import com.example.formbackend.model.EmailVerificationToken;
import com.example.formbackend.repository.UserRepository;
import com.example.formbackend.repository.EmailVerificationTokenRepository;
import com.example.formbackend.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.formbackend.model.Role;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByEmail(username).orElse(null);
    }

    public User registerUser(User user) throws IllegalArgumentException {
        if(userRepository.existsByEmail(user.getEmail())){
            throw new IllegalArgumentException("Email Already Exists");
        }
        // Convert role string to uppercase enum if role is set as string
        if (user.getRole() != null) {
            try {
                user.setRole(Role.valueOf(user.getRole().name().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role for registration");
            }
        } else {
            user.setRole(Role.CUSTOMER);
        }
        // Validate role: only CUSTOMER or AGENT allowed
        if (user.getRole() != Role.CUSTOMER && user.getRole() != Role.AGENT) {
            throw new IllegalArgumentException("Invalid role for registration");
        }
        // Set emailVerified to false initially
        user.setEmailVerified(false);
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        // Create and send verification token
        createVerificationToken(savedUser);
        return savedUser;
    }

    public void createVerificationToken(User user) {
        EmailVerificationToken token = new EmailVerificationToken(user);
        tokenRepository.save(token);
        // Send verification email
        emailSenderService.sendEmail(user.getEmail(), "Email Verification",
                "Please verify your email by clicking the link: " +
                        "http://localhost:8080/api/auth/verify-email?token=" + token.getToken());
    }

    public Optional<User> findByUsername(String username) {
       return userRepository.findByUsername(username);
    }

    // New method for password change
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            return false;
        }
        User user = optionalUser.get();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    // New method to get agents without tickets
    public List<User> getAgentsWithoutTickets() {
        return userRepository.findAgentsWithoutTickets();
    }

    // New method to get all agents
    public List<User> getAllAgents() {
        return userRepository.findAllAgents();
    }
}
