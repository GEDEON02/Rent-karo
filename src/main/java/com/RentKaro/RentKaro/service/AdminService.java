package com.RentKaro.RentKaro.service;

import com.RentKaro.RentKaro.dto.response.PropertyResponse;
import com.RentKaro.RentKaro.dto.response.UserResponse;
import com.RentKaro.RentKaro.exception.ResourceNotFoundException;
import com.RentKaro.RentKaro.model.*;
import com.RentKaro.RentKaro.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentRepository paymentRepository;

    // ─── User Management ───

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        propertyRepository.deleteByHostId(userId);
        userRepository.delete(user);
    }

    public UserResponse banUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsBanned(true);
        return mapToUserResponse(userRepository.save(user));
    }

    public UserResponse unbanUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsBanned(false);
        return mapToUserResponse(userRepository.save(user));
    }

    public UserResponse changeUserRole(String userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setRole(newRole);
        return mapToUserResponse(userRepository.save(user));
    }

    // ─── Property Management ───

    public List<PropertyResponse> getAllProperties() {
        return propertyRepository.findAll()
                .stream()
                .map(this::mapToPropertyResponse)
                .collect(Collectors.toList());
    }

    public PropertyResponse approveProperty(String propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        property.setApprovalStatus(ApprovalStatus.APPROVED);
        Property saved = propertyRepository.save(property);
        return mapToPropertyResponse(saved);
    }

    public PropertyResponse rejectProperty(String propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        property.setApprovalStatus(ApprovalStatus.REJECTED);
        Property saved = propertyRepository.save(property);
        return mapToPropertyResponse(saved);
    }

    public void deleteProperty(String propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        reviewRepository.deleteByListingId(propertyId);
        propertyRepository.delete(property);
    }

    // ─── Dashboard Stats ───

    public long getTotalUsers() { return userRepository.count(); }
    public long getTotalListings() { return propertyRepository.count(); }
    public long getTotalBookings() { return bookingRepository.count(); }
    public long getPendingProperties() { return propertyRepository.countByApprovalStatus(ApprovalStatus.PENDING); }
    public long getApprovedProperties() { return propertyRepository.countByApprovalStatus(ApprovalStatus.APPROVED); }
    public long getRejectedProperties() { return propertyRepository.countByApprovalStatus(ApprovalStatus.REJECTED); }

    public long getGuestCount() { return userRepository.countByRole(Role.GUEST); }
    public long getHostCount() { return userRepository.countByRole(Role.HOST); }
    public long getAdminCount() { return userRepository.countByRole(Role.ADMIN); }

    public long getConfirmedBookings() { return bookingRepository.findByStatus(BookingStatus.CONFIRMED).size(); }
    public long getPendingBookings() { return bookingRepository.findByStatus(BookingStatus.PENDING).size(); }
    public long getCancelledBookings() { return bookingRepository.findByStatus(BookingStatus.CANCELLED).size(); }
    public long getCompletedBookings() { return bookingRepository.findByStatus(BookingStatus.COMPLETED).size(); }

    public double getTotalRevenue() {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID)
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    public List<UserResponse> getRecentUsers(int limit) {
        List<UserResponse> all = getAllUsers();
        // Sort by createdAt desc and take top N
        all.sort((a, b) -> {
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        return all.subList(0, Math.min(limit, all.size()));
    }

    // ─── Mappers ───

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profilePicture(user.getProfilePicture())
                .role(user.getRole().name())
                .isVerified(user.getIsVerified())
                .isBanned(user.getIsBanned())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private PropertyResponse mapToPropertyResponse(Property property) {
        String hostEmail = "Unknown";
        String hostName = "Unknown";
        User host = userRepository.findById(property.getHostId()).orElse(null);
        if (host != null) {
            hostEmail = host.getEmail();
            hostName = host.getName();
        }

        List<Review> reviews = reviewRepository.findByListingId(property.getId());
        double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        return PropertyResponse.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .pricePerNight(property.getPricePerNight())
                .location(property.getLocation())
                .city(property.getCity())
                .country(property.getCountry())
                .maxGuests(property.getMaxGuests())
                .numBedrooms(property.getNumBedrooms())
                .numBathrooms(property.getNumBathrooms())
                .amenities(property.getAmenities())
                .images(property.getImages())
                .hostId(property.getHostId())
                .hostEmail(hostEmail)
                .hostName(hostName)
                .approvalStatus(property.getApprovalStatus().name())
                .averageRating(Math.round(avgRating * 10.0) / 10.0)
                .reviewCount(reviews.size())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .build();
    }
}
