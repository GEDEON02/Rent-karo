package com.RentKaro.RentKaro.controller;

import com.RentKaro.RentKaro.model.User;
import com.RentKaro.RentKaro.repository.NotificationRepository;
import com.RentKaro.RentKaro.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public GlobalModelAttributes(UserRepository userRepository, NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @ModelAttribute("currentUser")
    public String currentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return null;
    }

    @ModelAttribute("currentUserName")
    public String currentUserName(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return userRepository.findByEmail(authentication.getName())
                    .map(User::getName).orElse(authentication.getName());
        }
        return null;
    }

    @ModelAttribute("currentUserId")
    public String currentUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return userRepository.findByEmail(authentication.getName())
                    .map(User::getId).orElse(null);
        }
        return null;
    }

    @ModelAttribute("notificationCount")
    public long notificationCount(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                User user = userRepository.findByEmail(authentication.getName()).orElse(null);
                if (user != null) {
                    return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
                }
            } catch (Exception ignored) {}
        }
        return 0;
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities()
                    .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return false;
    }

    @ModelAttribute("isHost")
    public boolean isHost(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities()
                    .contains(new SimpleGrantedAuthority("ROLE_HOST"));
        }
        return false;
    }

    @ModelAttribute("isGuest")
    public boolean isGuest(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities()
                    .contains(new SimpleGrantedAuthority("ROLE_GUEST"));
        }
        return false;
    }
}
