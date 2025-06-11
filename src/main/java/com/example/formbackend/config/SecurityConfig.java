package com.example.formbackend.config;

import com.example.formbackend.security.JwtAuthenticationFilter;
import com.example.formbackend.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf(csrf -> csrf
                .ignoringRequestMatchers(
                    "/api/users/register",
                    "/api/auth/login",
                    "/api/tickets",
                    "/api/tickets/*/assign/*",
                    "/api/**"
                )
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                    "/api/auth/**",
                    "/api/auth/register"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/tickets").hasRole("CUSTOMER")
                .requestMatchers(HttpMethod.POST, "/api/comments/*").hasAnyRole("CUSTOMER", "AGENT")
                .requestMatchers(HttpMethod.POST, "/api/comments/ticket/*").hasAnyRole("CUSTOMER", "AGENT")
                .requestMatchers(HttpMethod.PUT, "/api/comments/*").hasAnyRole("CUSTOMER", "AGENT")
                .requestMatchers(HttpMethod.GET, "/api/comments/getcomments").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tickets/*/assign/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tickets/*/state").hasRole("AGENT")
                .requestMatchers(HttpMethod.GET, "/api/tickets/state").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")
                .anyRequest().authenticated()
            )
            .exceptionHandling()
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                System.out.println("Access denied: " + accessDeniedException.getMessage());
                response.sendError(403, "Access Denied");
            })
            .authenticationEntryPoint((request, response, authException) -> {
                System.out.println("Authentication failed: " + authException.getMessage());
                response.sendError(401, "Unauthorized");
            })
            .and()
            .authenticationProvider(userAuthenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider userAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // This is the recommended way to get AuthenticationManager built by Spring Security automatically,
    // which will use the AuthenticationProviders you configured.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
