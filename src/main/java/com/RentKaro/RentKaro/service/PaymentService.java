package com.RentKaro.RentKaro.service;

import com.RentKaro.RentKaro.dto.response.PaymentResponse;
import com.RentKaro.RentKaro.exception.ResourceNotFoundException;
import com.RentKaro.RentKaro.model.*;
import com.RentKaro.RentKaro.repository.BookingRepository;
import com.RentKaro.RentKaro.repository.PaymentRepository;
import com.RentKaro.RentKaro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Process 25% advance payment for a booking.
     */
    public PaymentResponse processAdvancePayment(Long bookingId, String paymentMethod, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getGuest().getId().equals(user.getId())) {
            throw new RuntimeException("You can only pay for your own bookings");
        }

        // Check if already paid
        if (paymentRepository.findByBooking_Id(bookingId).isPresent()) {
            throw new IllegalArgumentException("This booking has already been paid");
        }

        // Calculate 25% advance
        double advanceAmount = booking.getTotalPrice() * 0.25;

        // Simulate payment
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(advanceAmount)
                .paymentMethod(paymentMethod)
                .status(PaymentStatus.PAID)
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paidAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        // Update booking to confirmed
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Notify host
        Property listing = booking.getProperty();
        if (listing != null) {
            notificationService.createNotification(listing.getHost().getId(),
                    "Advance payment of ₹" + String.format("%.0f", advanceAmount) +
                    " received for \"" + listing.getTitle() + "\" (Total: ₹" +
                    String.format("%.0f", booking.getTotalPrice()) + ")");
        }

        return mapToResponse(saved);
    }

    public PaymentResponse processPayment(Long bookingId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getGuest().getId().equals(user.getId())) {
            throw new RuntimeException("You can only pay for your own bookings");
        }

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("This booking cannot be paid for");
        }

        // Check if already paid
        if (paymentRepository.findByBooking_Id(bookingId).isPresent()) {
            throw new IllegalArgumentException("This booking has already been paid");
        }

        // Simulate payment
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice())
                .paymentMethod("MOCK_CARD")
                .status(PaymentStatus.PAID)
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paidAt(LocalDateTime.now())
                .build();

        Payment saved = paymentRepository.save(payment);

        // Update booking to confirmed
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Notify host
        Property listing = booking.getProperty();
        if (listing != null) {
            notificationService.createNotification(listing.getHost().getId(),
                    "Payment received for booking \"" + listing.getTitle() + "\" — ₹" + payment.getAmount());
        }

        return mapToResponse(saved);
    }

    public PaymentResponse processRefund(Long bookingId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Payment payment = paymentRepository.findByBooking_Id(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("No payment found for this booking"));

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new IllegalArgumentException("Only paid bookings can be refunded");
        }

        // Simulate refund
        payment.setStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.save(payment);

        // Cancel booking
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Notify guest
        notificationService.createNotification(booking.getGuest().getId(),
                "Your payment of ₹" + String.format("%.0f", payment.getAmount()) + " has been refunded.");

        return mapToResponse(saved);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus().name())
                .transactionId(payment.getTransactionId())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
