package com.RentKaro.RentKaro.repository;

import com.RentKaro.RentKaro.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProperty_Id(Long propertyId);

    List<Review> findByGuest_Id(Long guestId);

    List<Review> findByBooking_Id(Long bookingId);

    void deleteByProperty_Id(Long propertyId);
}
