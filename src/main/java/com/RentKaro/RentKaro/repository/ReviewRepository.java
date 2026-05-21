package com.RentKaro.RentKaro.repository;

import com.RentKaro.RentKaro.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findByListingId(String listingId);

    List<Review> findByGuestId(String guestId);

    List<Review> findByBookingId(String bookingId);

    void deleteByListingId(String listingId);
}
