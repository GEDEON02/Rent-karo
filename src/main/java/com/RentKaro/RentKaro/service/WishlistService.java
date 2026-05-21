package com.RentKaro.RentKaro.service;

import com.RentKaro.RentKaro.dto.response.PropertyResponse;
import com.RentKaro.RentKaro.model.Property;
import com.RentKaro.RentKaro.model.Review;
import com.RentKaro.RentKaro.model.User;
import com.RentKaro.RentKaro.model.Wishlist;
import com.RentKaro.RentKaro.exception.ResourceNotFoundException;
import com.RentKaro.RentKaro.repository.PropertyRepository;
import com.RentKaro.RentKaro.repository.ReviewRepository;
import com.RentKaro.RentKaro.repository.UserRepository;
import com.RentKaro.RentKaro.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public boolean toggleWishlist(String email, Long listingId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Property property = propertyRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + listingId));

        return wishlistRepository.findByGuest_IdAndProperty_Id(user.getId(), listingId)
                .map(existing -> {
                    wishlistRepository.delete(existing);
                    return false;
                })
                .orElseGet(() -> {
                    wishlistRepository.save(Wishlist.builder()
                            .guest(user)
                            .property(property)
                            .build());
                    return true;
                });
    }

    public void removeFromWishlist(String email, Long listingId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        wishlistRepository.deleteByGuest_IdAndProperty_Id(user.getId(), listingId);
    }

    public List<PropertyResponse> getWishlist(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return wishlistRepository.findByGuest_Id(user.getId()).stream()
                .map(Wishlist::getProperty)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public boolean isInWishlist(String email, Long listingId) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;
        return wishlistRepository.findByGuest_IdAndProperty_Id(user.getId(), listingId).isPresent();
    }

    public Set<Long> getWishlistIds(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return Collections.emptySet();
        return wishlistRepository.findByGuest_Id(user.getId()).stream()
                .map(w -> w.getProperty().getId())
                .collect(Collectors.toCollection(HashSet::new));
    }

    private PropertyResponse mapToResponse(Property property) {
        String hostEmail = "Unknown";
        String hostName = "Unknown";
        Long hostId = null;
        User host = property.getHost();
        if (host != null) {
            hostId = host.getId();
            hostEmail = host.getEmail();
            hostName = host.getName();
        }
        List<Review> reviews = reviewRepository.findByProperty_Id(property.getId());
        double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        return PropertyResponse.builder()
                .id(property.getId()).title(property.getTitle()).description(property.getDescription())
                .pricePerNight(property.getPricePerNight()).location(property.getLocation())
                .city(property.getCity()).country(property.getCountry())
                .maxGuests(property.getMaxGuests()).numBedrooms(property.getNumBedrooms()).numBathrooms(property.getNumBathrooms())
                .amenities(property.getAmenities()).images(property.getImages())
                .hostId(hostId).hostEmail(hostEmail).hostName(hostName)
                .approvalStatus(property.getApprovalStatus().name())
                .averageRating(Math.round(avgRating * 10.0) / 10.0).reviewCount(reviews.size())
                .createdAt(property.getCreatedAt()).updatedAt(property.getUpdatedAt())
                .build();
    }
}
