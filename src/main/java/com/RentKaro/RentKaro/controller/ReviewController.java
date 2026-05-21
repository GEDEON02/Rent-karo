package com.RentKaro.RentKaro.controller;

import com.RentKaro.RentKaro.dto.request.ReviewRequest;
import com.RentKaro.RentKaro.dto.response.ApiResponse;
import com.RentKaro.RentKaro.dto.response.ReviewResponse;
import com.RentKaro.RentKaro.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/listings/{id}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        ReviewResponse response = reviewService.createReview(id, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review posted successfully", response));
    }

    @GetMapping("/api/listings/{id}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getListingReviews(@PathVariable Long id) {
        List<ReviewResponse> reviews = reviewService.getReviewsForListing(id);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved", reviews));
    }

    @DeleteMapping("/api/reviews/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable Long id,
            Authentication authentication) {
        reviewService.deleteReview(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Review deleted"));
    }
}
