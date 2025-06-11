package com.example.formbackend.service;

import com.example.formbackend.model.User;
import com.example.formbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

   @Override
public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    if (!user.isEmailVerified()) {
        throw new UsernameNotFoundException("Email not verified");
    }

    List<GrantedAuthority> authorities = List.of(
        new SimpleGrantedAuthority("ROLE_" + user.getRole().name()) // e.g., ROLE_CUSTOMER
    );

    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPassword(),
        authorities
    );
}

}
