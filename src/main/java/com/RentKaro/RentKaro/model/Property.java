package com.RentKaro.RentKaro.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "listings")
public class Property {

    @Id
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

    @Builder.Default
    private List<String> amenities = new ArrayList<>();

    @Builder.Default
    private List<String> images = new ArrayList<>();

    private String hostId;

    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
