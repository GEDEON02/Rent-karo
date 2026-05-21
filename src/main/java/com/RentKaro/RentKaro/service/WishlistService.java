package com.RentKaro.RentKaro.service;

import com.RentKaro.RentKaro.dto.response.PropertyResponse;
import com.RentKaro.RentKaro.model.Property;
import com.RentKaro.RentKaro.model.Review;
import com.RentKaro.RentKaro.model.User;
import com.RentKaro.RentKaro.model.Wishlist;
import com.RentKaro.RentKaro.repository.PropertyRepository;
import com.RentKaro.RentKaro.repository.ReviewRepository;
import com.RentKaro.RentKaro.repository.UserRepository;
import com.RentKaro.RentKaro.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public boolean toggleWishlist(String email, String listingId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findByGuestId(user.getId())
                .orElse(Wishlist.builder().guestId(user.getId()).listingIds(new ArrayList<>()).build());

        if (wishlist.getListingIds().contains(listingId)) {
            wishlist.getListingIds().remove(listingId);
            wishlistRepository.save(wishlist);
            return false;
        } else {
            wishlist.getListingIds().add(listingId);
            wishlistRepository.save(wishlist);
            return true;
        }
    }

    public List<PropertyResponse> getWishlist(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Wishlist wishlist = wishlistRepository.findByGuestId(user.getId()).orElse(null);
        if (wishlist == null || wishlist.getListingIds().isEmpty()) {
            return List.of();
        }

        return wishlist.getListingIds().stream()
                .map(propertyRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public boolean isInWishlist(String email, String listingId) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return false;
        Wishlist wishlist = wishlistRepository.findByGuestId(user.getId()).orElse(null);
        if (wishlist == null) return false;
        return wishlist.getListingIds().contains(listingId);
    }

    public Set<String> getWishlistIds(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return Collections.emptySet();
        Wishlist wishlist = wishlistRepository.findByGuestId(user.getId()).orElse(null);
        if (wishlist == null) return Collections.emptySet();
        return new HashSet<>(wishlist.getListingIds());
    }

    private PropertyResponse mapToResponse(Property property) {
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
                .id(property.getId()).title(property.getTitle()).description(property.getDescription())
                .pricePerNight(property.getPricePerNight()).location(property.getLocation())
                .city(property.getCity()).country(property.getCountry())
                .maxGuests(property.getMaxGuests()).numBedrooms(property.getNumBedrooms()).numBathrooms(property.getNumBathrooms())
                .amenities(property.getAmenities()).images(property.getImages())
                .hostId(property.getHostId()).hostEmail(hostEmail).hostName(hostName)
                .approvalStatus(property.getApprovalStatus().name())
                .averageRating(Math.round(avgRating * 10.0) / 10.0).reviewCount(reviews.size())
                .createdAt(property.getCreatedAt()).updatedAt(property.getUpdatedAt())
                .build();
    }
}
