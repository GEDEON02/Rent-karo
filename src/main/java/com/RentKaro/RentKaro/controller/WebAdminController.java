package com.RentKaro.RentKaro.controller;

import com.RentKaro.RentKaro.dto.response.*;
import com.RentKaro.RentKaro.model.Role;
import com.RentKaro.RentKaro.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class WebAdminController {

    private final AdminService adminService;
    private final BookingService bookingService;
    private final ReviewService reviewService;
    private final PaymentService paymentService;

    @GetMapping
    public String adminDashboard(Model model) {
        // Core stats
        model.addAttribute("totalUsers", adminService.getTotalUsers());
        model.addAttribute("totalListings", adminService.getTotalListings());
        model.addAttribute("totalBookings", adminService.getTotalBookings());
        model.addAttribute("pendingProperties", adminService.getPendingProperties());
        model.addAttribute("totalRevenue", adminService.getTotalRevenue());

        // Users by role
        model.addAttribute("guestCount", adminService.getGuestCount());
        model.addAttribute("hostCount", adminService.getHostCount());
        model.addAttribute("adminCount", adminService.getAdminCount());

        // Properties by status
        model.addAttribute("approvedProperties", adminService.getApprovedProperties());
        model.addAttribute("rejectedProperties", adminService.getRejectedProperties());

        // Bookings by status
        model.addAttribute("confirmedBookings", adminService.getConfirmedBookings());
        model.addAttribute("pendingBookings", adminService.getPendingBookings());
        model.addAttribute("cancelledBookings", adminService.getCancelledBookings());
        model.addAttribute("completedBookings", adminService.getCompletedBookings());

        // Recent users
        model.addAttribute("recentUsers", adminService.getRecentUsers(5));

        return "admin/dashboard";
    }

    // ─── Users ───

    @GetMapping("/users")
    public String usersPage(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/users";
    }

    @PostMapping("/delete-user/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminService.deleteUser(id);
            ra.addFlashAttribute("success", "User deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to delete user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/ban-user/{id}")
    public String banUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminService.banUser(id);
            ra.addFlashAttribute("success", "User banned.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to ban user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/unban-user/{id}")
    public String unbanUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminService.unbanUser(id);
            ra.addFlashAttribute("success", "User unbanned.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to unban user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/change-role/{id}")
    public String changeRole(@PathVariable Long id, @RequestParam(required = false) String role, RedirectAttributes ra) {
        try {
            if (role == null || role.isBlank()) {
                ra.addFlashAttribute("error", "Please select a role.");
                return "redirect:/admin/users";
            }
            adminService.changeUserRole(id, Role.valueOf(role.toUpperCase()));
            ra.addFlashAttribute("success", "Role changed to " + role.toUpperCase() + ".");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to change role: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // ─── Listings ───

    @GetMapping("/listings")
    public String listingsPage(Model model) {
        model.addAttribute("properties", adminService.getAllProperties());
        return "admin/listings";
    }

    @PostMapping("/approve-property/{id}")
    public String approveProperty(@PathVariable Long id, RedirectAttributes ra) {
        adminService.approveProperty(id);
        ra.addFlashAttribute("success", "Property approved!");
        return "redirect:/admin/listings";
    }

    @PostMapping("/reject-property/{id}")
    public String rejectProperty(@PathVariable Long id, RedirectAttributes ra) {
        adminService.rejectProperty(id);
        ra.addFlashAttribute("success", "Property rejected.");
        return "redirect:/admin/listings";
    }

    @PostMapping("/delete-property/{id}")
    public String deleteProperty(@PathVariable Long id, RedirectAttributes ra) {
        adminService.deleteProperty(id);
        ra.addFlashAttribute("success", "Property deleted.");
        return "redirect:/admin/listings";
    }

    // ─── Bookings ───

    @GetMapping("/bookings")
    public String bookingsPage(Model model) {
        model.addAttribute("bookings", bookingService.getAllBookings());
        return "admin/bookings";
    }

    @PostMapping("/cancel-booking/{id}")
    public String cancelBooking(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            // Admin can cancel any booking by using the service directly
            // For admin, we bypass the user check by setting up the booking directly
            var booking = bookingService.getAllBookings().stream()
                    .filter(b -> b.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Use guest email for cancellation (admin acts on behalf)
            bookingService.cancelBooking(id, booking.getGuestEmail());
            ra.addFlashAttribute("success", "Booking cancelled.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to cancel: " + e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    // ─── Reviews ───

    @GetMapping("/reviews")
    public String reviewsPage(Model model) {
        model.addAttribute("reviews", reviewService.getAllReviews());
        return "admin/reviews";
    }

    @PostMapping("/delete-review/{id}")
    public String deleteReview(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        reviewService.deleteReview(id, auth.getName());
        ra.addFlashAttribute("success", "Review deleted.");
        return "redirect:/admin/reviews";
    }

    // ─── Payments ───

    @GetMapping("/payments")
    public String paymentsPage(Model model) {
        model.addAttribute("payments", paymentService.getAllPayments());
        return "admin/payments";
    }
}
