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

    public ReviewResponse createReview(Long listingId, ReviewRequest request, String guestEmail) {
        User guest = userRepository.findByEmail(guestEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Property listing = propertyRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        // Business rule: guest can only review after a COMPLETED booking
        List<Booking> completedBookings = bookingRepository.findByGuest_Id(guest.getId())
                .stream()
                .filter(b -> b.getProperty().getId().equals(listingId) && b.getStatus() == BookingStatus.COMPLETED)
                .collect(Collectors.toList());

        if (completedBookings.isEmpty()) {
            throw new UnauthorizedException("You can only review a listing after a completed booking");
        }

        // Use the first completed booking
        Booking booking = completedBookings.get(0);

        // Check if already reviewed this booking
        List<Review> existingReviews = reviewRepository.findByBooking_Id(booking.getId());
        if (!existingReviews.isEmpty()) {
            throw new IllegalArgumentException("You have already reviewed this booking");
        }

        Review review = Review.builder()
                .guest(guest)
                .property(listing)
                .booking(booking)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        return mapToResponse(saved);
    }

    public List<ReviewResponse> getReviewsForListing(Long listingId) {
        return reviewRepository.findByProperty_Id(listingId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteReview(Long reviewId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        // Admin or the author can delete
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isAuthor = review.getGuest().getId().equals(user.getId());

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

    public double getAverageRating(Long listingId) {
        List<Review> reviews = reviewRepository.findByProperty_Id(listingId);
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    public int getReviewCount(Long listingId) {
        return reviewRepository.findByProperty_Id(listingId).size();
    }

    private ReviewResponse mapToResponse(Review review) {
        String guestName = "Unknown";
        String listingTitle = "Unknown";

        User guest = review.getGuest();
        if (guest != null) {
            guestName = guest.getName();
        }

        Property listing = review.getProperty();
        if (listing != null) {
            listingTitle = listing.getTitle();
        }

        return ReviewResponse.builder()
                .id(review.getId())
                .guestId(guest != null ? guest.getId() : null)
                .guestName(guestName)
                .listingId(listing != null ? listing.getId() : null)
                .listingTitle(listingTitle)
                .bookingId(review.getBooking() != null ? review.getBooking().getId() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
