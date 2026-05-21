package com.RentKaro.RentKaro.service;

import com.RentKaro.RentKaro.dto.request.BookingRequest;
import com.RentKaro.RentKaro.dto.response.BookingResponse;
import com.RentKaro.RentKaro.exception.ResourceNotFoundException;
import com.RentKaro.RentKaro.exception.UnauthorizedException;
import com.RentKaro.RentKaro.model.*;
import com.RentKaro.RentKaro.repository.BookingRepository;
import com.RentKaro.RentKaro.repository.PropertyRepository;
import com.RentKaro.RentKaro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public BookingResponse createBooking(BookingRequest request, String guestEmail) {
        User guest = userRepository.findByEmail(guestEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Property listing = propertyRepository.findById(request.getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        // Business rule: guest cannot book their own listing
        if (listing.getHostId().equals(guest.getId())) {
            throw new UnauthorizedException("You cannot book your own listing");
        }

        // Business rule: only approved listings can be booked
        if (listing.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new UnauthorizedException("This listing is not available for booking");
        }

        // Business rule: check-out must be after check-in
        if (!request.getCheckOut().isAfter(request.getCheckIn())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        // Business rule: check guest count
        if (request.getGuests() != null && listing.getMaxGuests() != null
                && request.getGuests() > listing.getMaxGuests()) {
            throw new IllegalArgumentException("Number of guests exceeds maximum capacity of " + listing.getMaxGuests());
        }

        // Business rule: no overlapping PENDING or CONFIRMED bookings
        List<Booking> overlapping = bookingRepository
                .findByListingIdAndStatusInAndCheckInBeforeAndCheckOutAfter(
                        request.getListingId(),
                        Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED),
                        request.getCheckOut(),
                        request.getCheckIn()
                );
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("The selected dates are already booked. Please choose different dates.");
        }

        // Auto-calculate price: nights × pricePerNight
        long nights = ChronoUnit.DAYS.between(request.getCheckIn(), request.getCheckOut());
        double totalPrice = nights * listing.getPricePerNight();

        Booking booking = Booking.builder()
                .guestId(guest.getId())
                .listingId(request.getListingId())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .totalPrice(totalPrice)
                .numberOfGuests(request.getGuests())
                .status(BookingStatus.PENDING) // Host must confirm the booking
                .build();

        Booking saved = bookingRepository.save(booking);

        // Notify host
        notificationService.createNotification(listing.getHostId(),
                "New booking for \"" + listing.getTitle() + "\" from " + guest.getName() +
                " (" + request.getCheckIn() + " to " + request.getCheckOut() + ")");

        return mapToResponse(saved);
    }

    public List<BookingResponse> getMyBookings(String guestEmail) {
        User guest = userRepository.findByEmail(guestEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return bookingRepository.findByGuestId(guest.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getHostBookings(String hostEmail) {
        User host = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<String> hostListingIds = propertyRepository.findByHostId(host.getId())
                .stream()
                .map(Property::getId)
                .collect(Collectors.toList());

        if (hostListingIds.isEmpty()) {
            return List.of();
        }

        return bookingRepository.findByListingIdIn(hostListingIds)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse cancelBooking(String bookingId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        // Both guest and host can cancel
        Property listing = propertyRepository.findById(booking.getListingId()).orElse(null);
        boolean isGuest = booking.getGuestId().equals(user.getId());
        boolean isHost = listing != null && listing.getHostId().equals(user.getId());

        if (!isGuest && !isHost) {
            throw new UnauthorizedException("You are not authorized to cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalArgumentException("This booking cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        // Notify the other party
        if (isGuest && listing != null) {
            notificationService.createNotification(listing.getHostId(),
                    "Booking for \"" + listing.getTitle() + "\" has been cancelled by the guest.");
        } else {
            notificationService.createNotification(booking.getGuestId(),
                    "Your booking has been cancelled by the host.");
        }

        return mapToResponse(saved);
    }

    public BookingResponse confirmBooking(String bookingId, String hostEmail) {
        User host = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Property listing = propertyRepository.findById(booking.getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!listing.getHostId().equals(host.getId())) {
            throw new UnauthorizedException("Only the host can confirm this booking");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalArgumentException("Only pending bookings can be confirmed");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        notificationService.createNotification(booking.getGuestId(),
                "Your booking for \"" + listing.getTitle() + "\" has been confirmed!");

        return mapToResponse(saved);
    }

    public BookingResponse completeBooking(String bookingId, String hostEmail) {
        User host = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        Property listing = propertyRepository.findById(booking.getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!listing.getHostId().equals(host.getId())) {
            throw new UnauthorizedException("Only the host can mark this booking as completed");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException("Only confirmed bookings can be marked as completed");
        }

        booking.setStatus(BookingStatus.COMPLETED);
        Booking saved = bookingRepository.save(booking);

        notificationService.createNotification(booking.getGuestId(),
                "Your stay at \"" + listing.getTitle() + "\" is now marked as completed. You can now leave a review!");

        return mapToResponse(saved);
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BookingResponse mapToResponse(Booking booking) {
        String guestName = "Unknown";
        String guestEmail = "Unknown";
        String listingTitle = "Unknown";

        User guest = userRepository.findById(booking.getGuestId()).orElse(null);
        if (guest != null) {
            guestName = guest.getName();
            guestEmail = guest.getEmail();
        }

        Property listing = propertyRepository.findById(booking.getListingId()).orElse(null);
        if (listing != null) {
            listingTitle = listing.getTitle();
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .guestId(booking.getGuestId())
                .guestName(guestName)
                .guestEmail(guestEmail)
                .listingId(booking.getListingId())
                .listingTitle(listingTitle)
                .checkIn(booking.getCheckIn())
                .checkOut(booking.getCheckOut())
                .totalPrice(booking.getTotalPrice())
                .advanceAmount(booking.getTotalPrice() * 0.25)
                .numberOfGuests(booking.getNumberOfGuests())
                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
