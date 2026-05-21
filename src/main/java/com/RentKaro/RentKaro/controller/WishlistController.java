package com.RentKaro.RentKaro.controller;

import com.RentKaro.RentKaro.dto.response.ApiResponse;
import com.RentKaro.RentKaro.dto.response.PropertyResponse;
import com.RentKaro.RentKaro.dto.response.WishlistResponse;
import com.RentKaro.RentKaro.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{listingId}")
    public ResponseEntity<ApiResponse<String>> addToWishlist(
            @PathVariable Long listingId,
            Authentication authentication) {
        boolean added = wishlistService.toggleWishlist(authentication.getName(), listingId);
        String message = added ? "Added to wishlist" : "Removed from wishlist";
        return ResponseEntity.ok(ApiResponse.success(message, message));
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(
            @PathVariable Long listingId,
            Authentication authentication) {
        wishlistService.removeFromWishlist(authentication.getName(), listingId);
        return ResponseEntity.ok(ApiResponse.success("Removed from wishlist", "Removed from wishlist"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getMyWishlist(Authentication authentication) {
        List<PropertyResponse> properties = wishlistService.getWishlist(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Wishlist retrieved", properties));
    }
}
