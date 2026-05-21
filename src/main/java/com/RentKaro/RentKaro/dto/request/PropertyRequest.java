package com.RentKaro.RentKaro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotNull(message = "Price per night is required")
    @Positive(message = "Price must be a positive number")
    private Double pricePerNight;

    @NotBlank(message = "Location is required")
    @Size(min = 2, max = 200, message = "Location must be between 2 and 200 characters")
    private String location;

    private String city;

    private String country;

    private Double latitude;

    private Double longitude;

    @Positive(message = "Max guests must be positive")
    private Integer maxGuests;

    @Positive(message = "Number of bedrooms must be positive")
    private Integer numBedrooms;

    @Positive(message = "Number of bathrooms must be positive")
    private Integer numBathrooms;

    private List<String> amenities;

    private List<String> images;
}
