package com.RentKaro.RentKaro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponse {

    private String id;
    private String title;
    private String description;
    private Double pricePerNight;
    private String location;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private Integer maxGuests;
    private Integer numBedrooms;
    private Integer numBathrooms;
    private List<String> amenities;
    private List<String> images;
    private String hostId;
    private String hostEmail;
    private String hostName;
    private String approvalStatus;
    private Double averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
