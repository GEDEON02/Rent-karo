package com.RentKaro.RentKaro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private String id;
    private String guestId;
    private String guestName;
    private String guestEmail;
    private String listingId;
    private String listingTitle;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Double totalPrice;
    private Double advanceAmount;
    private Integer numberOfGuests;
    private String status;
    private LocalDateTime createdAt;
}
