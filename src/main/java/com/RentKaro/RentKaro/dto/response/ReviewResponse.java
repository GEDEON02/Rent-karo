package com.RentKaro.RentKaro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private String id;
    private String guestId;
    private String guestName;
    private String listingId;
    private String listingTitle;
    private String bookingId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
