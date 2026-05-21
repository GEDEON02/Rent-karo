package com.RentKaro.RentKaro.controller;

import com.RentKaro.RentKaro.dto.request.PropertyRequest;
import com.RentKaro.RentKaro.dto.response.BookingResponse;
import com.RentKaro.RentKaro.dto.response.NotificationResponse;
import com.RentKaro.RentKaro.dto.response.PropertyResponse;
import com.RentKaro.RentKaro.model.User;
import com.RentKaro.RentKaro.repository.NotificationRepository;
import com.RentKaro.RentKaro.repository.UserRepository;
import com.RentKaro.RentKaro.service.BookingService;
import com.RentKaro.RentKaro.service.NotificationService;

import com.RentKaro.RentKaro.service.PropertyService;
import com.RentKaro.RentKaro.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class WebDashboardController {

    private final PropertyService propertyService;
    private final BookingService bookingService;
    private final WishlistService wishlistService;
    private final NotificationService notificationService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ═══════════════════════════════════════════
    //  DASHBOARD HOME
    // ═══════════════════════════════════════════

    @GetMapping
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();

        // Redirect admin to admin panel
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return "redirect:/admin";
        }

        List<PropertyResponse> properties = propertyService.getMyListings(email);
        List<BookingResponse> myBookings = bookingService.getMyBookings(email);
        List<BookingResponse> hostBookings = bookingService.getHostBookings(email);

        long approvedCount = properties.stream()
                .filter(p -> "APPROVED".equals(p.getApprovalStatus())).count();
        long pendingCount = properties.stream()
                .filter(p -> "PENDING".equals(p.getApprovalStatus())).count();
        long rejectedCount = properties.stream()
                .filter(p -> "REJECTED".equals(p.getApprovalStatus())).count();

        // Separate bookings into upcoming and past
        List<BookingResponse> upcomingBookings = myBookings.stream()
                .filter(b -> b.getCheckOut() != null && !b.getCheckOut().isBefore(java.time.LocalDate.now()))
                .collect(Collectors.toList());
        List<BookingResponse> pastBookings = myBookings.stream()
                .filter(b -> b.getCheckOut() != null && b.getCheckOut().isBefore(java.time.LocalDate.now()))
                .collect(Collectors.toList());

        // Host earnings
        double totalEarnings = hostBookings.stream()
                .filter(b -> "CONFIRMED".equals(b.getStatus()) || "COMPLETED".equals(b.getStatus()))
                .mapToDouble(b -> b.getAdvanceAmount() != null ? b.getAdvanceAmount() : 0)
                .sum();

        model.addAttribute("properties", properties);
        model.addAttribute("myBookings", myBookings);
        model.addAttribute("upcomingBookings", upcomingBookings);
        model.addAttribute("pastBookings", pastBookings);
        model.addAttribute("hostBookings", hostBookings);
        model.addAttribute("totalListings", properties.size());
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("totalEarnings", totalEarnings);

        return "dashboard";
    }

    // ═══════════════════════════════════════════
    //  PROFILE
    // ═══════════════════════════════════════════

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String profilePicture,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setName(name);
            user.setPhone(phone);
            user.setProfilePicture(profilePicture);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            if (newPassword.length() < 6) {
                throw new RuntimeException("New password must be at least 6 characters");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/profile";
    }

    // ═══════════════════════════════════════════
    //  WISHLIST
    // ═══════════════════════════════════════════

    @GetMapping("/wishlist")
    public String wishlistPage(Authentication authentication, Model model) {
        List<PropertyResponse> properties = wishlistService.getWishlist(authentication.getName());
        model.addAttribute("properties", properties);
        return "wishlist";
    }

    @PostMapping("/wishlist/toggle/{id}")
    public String toggleWishlist(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes,
                                 @RequestParam(required = false) String returnUrl) {
        boolean added = wishlistService.toggleWishlist(authentication.getName(), id);
        redirectAttributes.addFlashAttribute("success",
                added ? "Added to wishlist!" : "Removed from wishlist.");
        if (returnUrl != null && !returnUrl.isBlank()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/dashboard/wishlist";
    }

    // ═══════════════════════════════════════════
    //  NOTIFICATIONS
    // ═══════════════════════════════════════════

    @GetMapping("/notifications")
    public String notificationsPage(Authentication authentication, Model model) {
        List<NotificationResponse> notifications = notificationService.getMyNotifications(authentication.getName());
        long unreadCount = notificationService.getUnreadCount(authentication.getName());
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        return "notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markAsRead(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        notificationService.markAsRead(id, authentication.getName());
        return "redirect:/dashboard/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllAsRead(Authentication authentication, RedirectAttributes redirectAttributes) {
        List<NotificationResponse> notifications = notificationService.getMyNotifications(authentication.getName());
        for (NotificationResponse n : notifications) {
            if (n.getIsRead() == null || !n.getIsRead()) {
                try { notificationService.markAsRead(n.getId(), authentication.getName()); } catch (Exception ignored) {}
            }
        }
        redirectAttributes.addFlashAttribute("success", "All notifications marked as read.");
        return "redirect:/dashboard/notifications";
    }

    // ═══════════════════════════════════════════
    //  BOOKING ACTIONS
    // ═══════════════════════════════════════════

    @PostMapping("/cancel-booking/{id}")
    public String cancelBooking(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/confirm-booking/{id}")
    public String confirmBooking(@PathVariable Long id,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            bookingService.confirmBooking(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Booking confirmed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/complete-booking/{id}")
    public String completeBooking(@PathVariable Long id,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            bookingService.completeBooking(id, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Booking marked as completed! The guest can now leave a review.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    // ═══════════════════════════════════════════
    //  PROPERTY CRUD
    // ═══════════════════════════════════════════

    @GetMapping("/add-property")
    public String addPropertyPage(Authentication authentication, RedirectAttributes redirectAttributes) {
        boolean isHost = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HOST"));
        if (!isHost) {
            redirectAttributes.addFlashAttribute("error", "Only registered Hosts can list properties. Please contact admin to upgrade your account to Host.");
            return "redirect:/dashboard";
        }
        return "add-property";
    }

    @PostMapping("/add-property")
    public String addProperty(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Double pricePerNight,
            @RequestParam String location,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer maxGuests,
            @RequestParam(required = false) Integer numBedrooms,
            @RequestParam(required = false) Integer numBathrooms,
            @RequestParam(required = false) String amenities,
            @RequestParam(required = false) String imageUrls,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            List<String> amenityList = new ArrayList<>();
            if (amenities != null && !amenities.isBlank()) {
                for (String a : amenities.split(",")) {
                    amenityList.add(a.trim());
                }
            }

            List<String> imageList = new ArrayList<>();
            if (imageUrls != null && !imageUrls.isBlank()) {
                for (String u : imageUrls.split("\n")) {
                    String trimmed = u.trim();
                    if (!trimmed.isEmpty()) imageList.add(trimmed);
                }
            }

            PropertyRequest request = new PropertyRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setPricePerNight(pricePerNight);
            request.setLocation(location);
            request.setCity(city);
            request.setCountry(country);
            request.setMaxGuests(maxGuests);
            request.setNumBedrooms(numBedrooms);
            request.setNumBathrooms(numBathrooms);
            request.setAmenities(amenityList);
            request.setImages(imageList);

            propertyService.createProperty(request, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Property listed successfully! Pending admin approval.");
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "add-property";
        }
    }

    @GetMapping("/edit-property/{id}")
    public String editPropertyPage(@PathVariable Long id, Authentication authentication, Model model) {
        PropertyResponse property = propertyService.getPropertyById(id);
        model.addAttribute("property", property);
        return "edit-property";
    }

    @PostMapping("/edit-property/{id}")
    public String editProperty(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam Double pricePerNight,
            @RequestParam String location,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer maxGuests,
            @RequestParam(required = false) Integer numBedrooms,
            @RequestParam(required = false) Integer numBathrooms,
            @RequestParam(required = false) String amenities,
            @RequestParam(required = false) String imageUrls,
            Authentication authentication,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {
            List<String> amenityList = new ArrayList<>();
            if (amenities != null && !amenities.isBlank()) {
                for (String a : amenities.split(",")) {
                    amenityList.add(a.trim());
                }
            }

            List<String> imageList = new ArrayList<>();
            if (imageUrls != null && !imageUrls.isBlank()) {
                for (String u : imageUrls.split("\n")) {
                    String trimmed = u.trim();
                    if (!trimmed.isEmpty()) imageList.add(trimmed);
                }
            }

            PropertyRequest request = new PropertyRequest();
            request.setTitle(title);
            request.setDescription(description);
            request.setPricePerNight(pricePerNight);
            request.setLocation(location);
            request.setCity(city);
            request.setCountry(country);
            request.setMaxGuests(maxGuests);
            request.setNumBedrooms(numBedrooms);
            request.setNumBathrooms(numBathrooms);
            request.setAmenities(amenityList);
            request.setImages(imageList);

            propertyService.updateProperty(id, request, authentication.getName());
            redirectAttributes.addFlashAttribute("success", "Property updated! Pending re-approval.");
            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("property", propertyService.getPropertyById(id));
            return "edit-property";
        }
    }

    @PostMapping("/delete-property/{id}")
    public String deleteProperty(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        propertyService.deleteProperty(id, authentication.getName());
        redirectAttributes.addFlashAttribute("success", "Property deleted successfully.");
        return "redirect:/dashboard";
    }
}
