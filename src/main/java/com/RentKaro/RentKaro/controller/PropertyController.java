package com.RentKaro.RentKaro.controller;

import com.RentKaro.RentKaro.dto.request.PropertyRequest;
import com.RentKaro.RentKaro.dto.response.ApiResponse;
import com.RentKaro.RentKaro.dto.response.PropertyResponse;
import com.RentKaro.RentKaro.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @Valid @RequestBody PropertyRequest request,
            Authentication authentication) {
        PropertyResponse response = propertyService.createProperty(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Property created successfully. Pending admin approval.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAllApprovedProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Double priceMin,
            @RequestParam(required = false) Double priceMax,
            @RequestParam(required = false) Integer guests) {

        List<PropertyResponse> properties;
        if (city != null || country != null || priceMin != null || priceMax != null || guests != null) {
            properties = propertyService.searchListings(city, country, priceMin, priceMax, guests);
        } else {
            properties = propertyService.getAllApprovedProperties();
        }

        return ResponseEntity.ok(ApiResponse.success("Properties retrieved", properties));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> getPropertyById(@PathVariable String id) {
        PropertyResponse response = propertyService.getPropertyById(id);
        return ResponseEntity.ok(ApiResponse.success("Property retrieved", response));
    }

    @GetMapping("/my-listings")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getMyListings(Authentication authentication) {
        List<PropertyResponse> properties = propertyService.getMyListings(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Your listings retrieved", properties));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @PathVariable String id,
            @Valid @RequestBody PropertyRequest request,
            Authentication authentication) {
        PropertyResponse response = propertyService.updateProperty(id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Property updated successfully. Pending re-approval.", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProperty(
            @PathVariable String id,
            Authentication authentication) {
        propertyService.deleteProperty(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("Property deleted successfully"));
    }
}
