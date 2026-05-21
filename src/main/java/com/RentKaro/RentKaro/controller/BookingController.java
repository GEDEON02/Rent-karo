package com.RentKaro.RentKaro.controller;

import com.RentKaro.RentKaro.dto.request.BookingRequest;
import com.RentKaro.RentKaro.dto.response.ApiResponse;
import com.RentKaro.RentKaro.dto.response.BookingResponse;
import com.RentKaro.RentKaro.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        BookingResponse response = bookingService.createBooking(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(Authentication authentication) {
        List<BookingResponse> bookings = bookingService.getMyBookings(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Your bookings retrieved", bookings));
    }

    @GetMapping("/host")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getHostBookings(Authentication authentication) {
        List<BookingResponse> bookings = bookingService.getHostBookings(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Host bookings retrieved", bookings));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {
        BookingResponse response = bookingService.cancelBooking(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled", response));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @PathVariable Long id,
            Authentication authentication) {
        BookingResponse response = bookingService.confirmBooking(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed", response));
    }
}
