package com.RentKaro.RentKaro.controller;

import com.RentKaro.RentKaro.dto.response.ApiResponse;
import com.RentKaro.RentKaro.dto.response.BookingResponse;
import com.RentKaro.RentKaro.dto.response.PaymentResponse;
import com.RentKaro.RentKaro.dto.response.PropertyResponse;
import com.RentKaro.RentKaro.dto.response.ReviewResponse;
import com.RentKaro.RentKaro.dto.response.UserResponse;
import com.RentKaro.RentKaro.model.Role;
import com.RentKaro.RentKaro.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final BookingService bookingService;
    private final ReviewService reviewService;
    private final PaymentService paymentService;

    // ─── Users ───

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("All users retrieved", adminService.getAllUsers()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @PutMapping("/users/{id}/ban")
    public ResponseEntity<ApiResponse<UserResponse>> banUser(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("User banned", adminService.banUser(id)));
    }

    @PutMapping("/users/{id}/unban")
    public ResponseEntity<ApiResponse<UserResponse>> unbanUser(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("User unbanned", adminService.unbanUser(id)));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> changeRole(
            @PathVariable String id, @RequestParam String role) {
        Role newRole = Role.valueOf(role.toUpperCase());
        return ResponseEntity.ok(ApiResponse.success("Role changed", adminService.changeUserRole(id, newRole)));
    }

    // ─── Properties ───

    @GetMapping("/properties")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAllProperties() {
        return ResponseEntity.ok(ApiResponse.success("All properties retrieved", adminService.getAllProperties()));
    }

    @PutMapping("/properties/{id}/approve")
    public ResponseEntity<ApiResponse<PropertyResponse>> approveProperty(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Property approved", adminService.approveProperty(id)));
    }

    @PutMapping("/properties/{id}/reject")
    public ResponseEntity<ApiResponse<PropertyResponse>> rejectProperty(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success("Property rejected", adminService.rejectProperty(id)));
    }

    @DeleteMapping("/properties/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(@PathVariable String id) {
        adminService.deleteProperty(id);
        return ResponseEntity.ok(ApiResponse.success("Property deleted"));
    }

    // ─── Bookings ───

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        return ResponseEntity.ok(ApiResponse.success("All bookings retrieved", bookingService.getAllBookings()));
    }

    // ─── Reviews ───

    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getAllReviews() {
        return ResponseEntity.ok(ApiResponse.success("All reviews retrieved", reviewService.getAllReviews()));
    }

    // ─── Payments ───

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.success("All payments retrieved", paymentService.getAllPayments()));
    }
}
