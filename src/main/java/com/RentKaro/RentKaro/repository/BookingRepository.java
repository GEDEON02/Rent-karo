package com.RentKaro.RentKaro.repository;

import com.RentKaro.RentKaro.model.Booking;
import com.RentKaro.RentKaro.model.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    List<Booking> findByGuestId(String guestId);

    List<Booking> findByListingId(String listingId);

    List<Booking> findByListingIdIn(List<String> listingIds);

    List<Booking> findByStatus(BookingStatus status);

    // Original: checks single status
    List<Booking> findByListingIdAndStatusAndCheckInBeforeAndCheckOutAfter(
            String listingId, BookingStatus status, LocalDate checkOut, LocalDate checkIn);

    // New: checks multiple statuses (PENDING + CONFIRMED) for overlap detection
    List<Booking> findByListingIdAndStatusInAndCheckInBeforeAndCheckOutAfter(
            String listingId, List<BookingStatus> statuses, LocalDate checkOut, LocalDate checkIn);
}
