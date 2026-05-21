package com.RentKaro.RentKaro.service;

import com.RentKaro.RentKaro.dto.request.LoginRequest;
import com.RentKaro.RentKaro.dto.request.RegisterRequest;
import com.RentKaro.RentKaro.dto.response.AuthResponse;
import com.RentKaro.RentKaro.exception.DuplicateResourceException;
import com.RentKaro.RentKaro.model.Role;
import com.RentKaro.RentKaro.model.User;
import com.RentKaro.RentKaro.repository.UserRepository;
import com.RentKaro.RentKaro.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // Determine role — default to GUEST
        Role role = Role.GUEST;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("HOST")) {
            role = Role.HOST;
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Registration successful")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user is banned
        if (user.getIsBanned() != null && user.getIsBanned()) {
            throw new RuntimeException("Your account has been banned. Contact admin.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }
}
