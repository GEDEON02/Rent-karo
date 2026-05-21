package com.RentKaro.RentKaro.controller;

import com.RentKaro.RentKaro.exception.DuplicateResourceException;
import com.RentKaro.RentKaro.model.Role;
import com.RentKaro.RentKaro.model.User;
import com.RentKaro.RentKaro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class WebAuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "GUEST") String role,
            Model model) {

        try {
            if (userRepository.existsByEmail(email)) {
                throw new DuplicateResourceException("Email already registered");
            }

            Role userRole = Role.GUEST;
            if ("HOST".equalsIgnoreCase(role)) {
                userRole = Role.HOST;
            }

            User user = User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(userRole)
                    .build();

            userRepository.save(user);

            return "redirect:/login?registered=true";
        } catch (DuplicateResourceException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. Please try again.");
            return "register";
        }
    }
}
