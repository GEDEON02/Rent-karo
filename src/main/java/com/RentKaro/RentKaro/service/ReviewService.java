package com.RentKaro.RentKaro.service;

import com.RentKaro.RentKaro.dto.request.ReviewRequest;
import com.RentKaro.RentKaro.dto.response.ReviewResponse;
import com.RentKaro.RentKaro.exception.ResourceNotFoundException;
import com.RentKaro.RentKaro.exception.UnauthorizedException;
import com.RentKaro.RentKaro.model.*;
import com.RentKaro.RentKaro.repository.BookingRepository;
import com.RentKaro.RentKaro.repository.PropertyRepository;
import com.RentKaro.RentKaro.repository.ReviewRepository;
import com.RentKaro.RentKaro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public ReviewResponse createReview(String listingId, ReviewRequest request, String guestEmail) {
        User guest = userRepository.findByEmail(guestEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Property listing = propertyRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        // Business rule: guest can only review after a COMPLETED booking
        List<Booking> completedBookings = bookingRepository.findByGuestId(guest.getId())
                .stream()
                .filter(b -> b.getListingId().equals(listingId) && b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.toList());

        if (completedBookings.isEmpty()) {
            throw new UnauthorizedException("You can only review a listing after a completed booking");
        }

        // Use the first completed booking
        Booking booking = completedBookings.get(0);

        // Check if already reviewed this booking
        List<Review> existingReviews = reviewRepository.findByBookingId(booking.getId());
        if (!existingReviews.isEmpty()) {
            throw new IllegalArgumentException("You have already reviewed this booking");
        }

        Review review = Review.builder()
                .guestId(guest.getId())
                .listingId(listingId)
                .bookingId(booking.getId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    public List<ReviewResponse> getReviewsForListing(String listingId) {
        return reviewRepository.findByListingId(listingId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteReview(String reviewId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Admin or the author can delete
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isAuthor = review.getGuestId().equals(user.getId());

        if (!isAdmin && !isAuthor) {
            throw new UnauthorizedException("You are not authorized to delete this review");
        }

        reviewRepository.delete(review);
    }

    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public double getAverageRating(String listingId) {
        List<Review> reviews = reviewRepository.findByListingId(listingId);
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public int getReviewCount(String listingId) {
        return reviewRepository.findByListingId(listingId).size();
    }

    private ReviewResponse mapToResponse(Review review) {
        String guestName = "Unknown";
        String listingTitle = "Unknown";

        User guest = userRepository.findById(review.getGuestId()).orElse(null);
        if (guest != null) {
            guestName = guest.getName();
        }

        Property listing = propertyRepository.findById(review.getListingId()).orElse(null);
        if (listing != null) {
            listingTitle = listing.getTitle();
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .guestId(review.getGuestId())
                .guestName(guestName)
                .listingId(review.getListingId())
                .listingTitle(listingTitle)
                .bookingId(review.getBookingId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
