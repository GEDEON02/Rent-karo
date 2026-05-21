package com.RentKaro.RentKaro.service;

import com.RentKaro.RentKaro.dto.request.PropertyRequest;
import com.RentKaro.RentKaro.dto.response.PropertyResponse;
import com.RentKaro.RentKaro.exception.ResourceNotFoundException;
import com.RentKaro.RentKaro.exception.UnauthorizedException;
import com.RentKaro.RentKaro.model.ApprovalStatus;
import com.RentKaro.RentKaro.model.Property;
import com.RentKaro.RentKaro.model.Review;
import com.RentKaro.RentKaro.model.User;
import com.RentKaro.RentKaro.repository.PropertyRepository;
import com.RentKaro.RentKaro.repository.ReviewRepository;
import com.RentKaro.RentKaro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public PropertyResponse createProperty(PropertyRequest request, String hostEmail) {
        User host = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Property property = Property.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .pricePerNight(request.getPricePerNight())
                .location(request.getLocation())
                .city(request.getCity())
                .country(request.getCountry())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .maxGuests(request.getMaxGuests())
                .numBedrooms(request.getNumBedrooms())
                .numBathrooms(request.getNumBathrooms())
                .amenities(request.getAmenities() != null ? request.getAmenities() : new ArrayList<>())
                .images(request.getImages() != null ? request.getImages() : new ArrayList<>())
                .host(host)
                .approvalStatus(ApprovalStatus.PENDING)
                .build();

        Property saved = propertyRepository.save(property);
        return mapToResponse(saved);
    }

    public List<PropertyResponse> getAllApprovedProperties() {
        return propertyRepository.findByApprovalStatus(ApprovalStatus.APPROVED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PropertyResponse> searchListings(String city, String country,
                                                  Double priceMin, Double priceMax,
                                                  Integer guests) {
        List<Property> results = propertyRepository.findByApprovalStatus(ApprovalStatus.APPROVED);

        return results.stream()
                .filter(p -> city == null || city.isEmpty() ||
                        (p.getCity() != null && p.getCity().toLowerCase().contains(city.toLowerCase())))
                .filter(p -> country == null || country.isEmpty() ||
                        (p.getCountry() != null && p.getCountry().toLowerCase().contains(country.toLowerCase())))
                .filter(p -> priceMin == null || p.getPricePerNight() >= priceMin)
                .filter(p -> priceMax == null || p.getPricePerNight() <= priceMax)
                .filter(p -> guests == null || (p.getMaxGuests() != null && p.getMaxGuests() >= guests))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PropertyResponse getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        return mapToResponse(property);
    }

    public List<PropertyResponse> getMyListings(String hostEmail) {
        User host = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return propertyRepository.findByHost_Id(host.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PropertyResponse updateProperty(Long id, PropertyRequest request, String hostEmail) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));

        User host = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!property.getHost().getId().equals(host.getId())) {
            throw new UnauthorizedException("You can only update your own properties");
        }

        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setPricePerNight(request.getPricePerNight());
        property.setLocation(request.getLocation());
        property.setCity(request.getCity());
        property.setCountry(request.getCountry());
        property.setLatitude(request.getLatitude());
        property.setLongitude(request.getLongitude());
        property.setMaxGuests(request.getMaxGuests());
        property.setNumBedrooms(request.getNumBedrooms());
        property.setNumBathrooms(request.getNumBathrooms());
        if (request.getAmenities() != null) property.setAmenities(request.getAmenities());
        if (request.getImages() != null) property.setImages(request.getImages());
        // Reset approval status when property is updated
        property.setApprovalStatus(ApprovalStatus.PENDING);

        Property updated = propertyRepository.save(property);
        return mapToResponse(updated);
    }

    public void deleteProperty(Long id, String hostEmail) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));

        User host = userRepository.findByEmail(hostEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!property.getHost().getId().equals(host.getId())) {
            throw new UnauthorizedException("You can only delete your own properties");
        }

        propertyRepository.delete(property);
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
        int reviewCount = reviews.size();

        return PropertyResponse.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .pricePerNight(property.getPricePerNight())
                .location(property.getLocation())
                .city(property.getCity())
                .country(property.getCountry())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .maxGuests(property.getMaxGuests())
                .numBedrooms(property.getNumBedrooms())
                .numBathrooms(property.getNumBathrooms())
                .amenities(property.getAmenities())
                .images(property.getImages())
                .hostId(hostId)
                .hostEmail(hostEmail)
                .hostName(hostName)
                .approvalStatus(property.getApprovalStatus().name())
                .averageRating(Math.round(avgRating * 10.0) / 10.0)
                .reviewCount(reviewCount)
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .build();
    }
}
