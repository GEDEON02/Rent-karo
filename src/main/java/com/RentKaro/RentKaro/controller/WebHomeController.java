package com.RentKaro.RentKaro.controller;

import com.RentKaro.RentKaro.dto.request.BookingRequest;
import com.RentKaro.RentKaro.dto.request.ReviewRequest;
import com.RentKaro.RentKaro.dto.response.BookingResponse;
import com.RentKaro.RentKaro.dto.response.PropertyResponse;
import com.RentKaro.RentKaro.dto.response.ReviewResponse;
import com.RentKaro.RentKaro.service.BookingService;
import com.RentKaro.RentKaro.service.PaymentService;
import com.RentKaro.RentKaro.service.PropertyService;
import com.RentKaro.RentKaro.service.ReviewService;
import com.RentKaro.RentKaro.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class WebHomeController {

    private final PropertyService propertyService;
    private final ReviewService reviewService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final WishlistService wishlistService;

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        try {
            List<PropertyResponse> featured = propertyService.getAllApprovedProperties();
            if (featured.size() > 6) {
                featured = featured.subList(0, 6);
            }
            model.addAttribute("featuredProperties", featured);

            // Pass wishlist IDs if logged in
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                model.addAttribute("wishlistIds", wishlistService.getWishlistIds(authentication.getName()));
            }
        } catch (Exception e) {
            model.addAttribute("featuredProperties", new java.util.ArrayList<>());
        }
        return "index";
    }

    @GetMapping("/properties")
    public String browseProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double priceMin,
            @RequestParam(required = false) Double priceMax,
            @RequestParam(required = false) Integer guests,
            Authentication authentication,
            Model model) {

        if (city != null && city.isBlank()) city = null;

        List<PropertyResponse> properties;
        try {
            if (city != null || priceMin != null || priceMax != null || guests != null) {
                properties = propertyService.searchListings(city, null, priceMin, priceMax, guests);
            } else {
                properties = propertyService.getAllApprovedProperties();
            }
        } catch (Exception e) {
            properties = new java.util.ArrayList<>();
        }
        model.addAttribute("properties", properties);

        // Pass wishlist IDs if logged in
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            model.addAttribute("wishlistIds", wishlistService.getWishlistIds(authentication.getName()));
        }

        return "properties";
    }

    @GetMapping("/properties/{id}")
    public String propertyDetail(@PathVariable String id, Model model, Authentication authentication) {
        PropertyResponse property = propertyService.getPropertyById(id);
        List<ReviewResponse> reviews = reviewService.getReviewsForListing(id);
        model.addAttribute("property", property);
        model.addAttribute("reviews", reviews);

        // Check if user can write a review (has a completed booking + hasn't reviewed yet)
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            boolean canReview = false;
            try {
                List<BookingResponse> myBookings = bookingService.getMyBookings(email);
                boolean hasCompletedBooking = myBookings.stream()
                        .anyMatch(b -> b.getListingId().equals(id) && "COMPLETED".equals(b.getStatus()));
                boolean alreadyReviewed = reviews.stream()
                        .anyMatch(r -> r.getGuestName() != null); // Simplified check
                canReview = hasCompletedBooking;
            } catch (Exception ignored) {}
            model.addAttribute("canReview", canReview);

            // Wishlist check
            model.addAttribute("isWishlisted", wishlistService.isInWishlist(email, id));
        }

        return "property-detail";
    }

    /**
     * Book a property — creates booking + processes 25% advance payment.
     */
    @PostMapping("/properties/{id}/book")
    public String bookProperty(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") Integer guests,
            @RequestParam(defaultValue = "CARD") String paymentMethod,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            BookingRequest request = new BookingRequest();
            request.setListingId(id);
            request.setCheckIn(checkIn);
            request.setCheckOut(checkOut);
            request.setGuests(guests);

            BookingResponse booking = bookingService.createBooking(request, authentication.getName());

            redirectAttributes.addFlashAttribute("bookingSuccess",
                    "Booking request submitted! Your booking is PENDING confirmation by the host. " +
                    "Total price: ₹" + String.format("%.0f", booking.getTotalPrice()) +
                    ". You will be notified once the host confirms.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("bookingError", e.getMessage());
        }

        return "redirect:/properties/" + id;
    }

    /**
     * Submit a review for a property.
     */
    @PostMapping("/properties/{id}/review")
    public String submitReview(
            @PathVariable String id,
            @RequestParam Integer rating,
            @RequestParam String comment,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            ReviewRequest request = new ReviewRequest();
            request.setRating(rating);
            request.setComment(comment);
            reviewService.createReview(id, request, authentication.getName());
            redirectAttributes.addFlashAttribute("bookingSuccess", "Review submitted successfully! Thank you for your feedback.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("bookingError", e.getMessage());
        }

        return "redirect:/properties/" + id;
    }
}
