package com.RentKaro.RentKaro.repository;

import com.RentKaro.RentKaro.model.Booking;
import com.RentKaro.RentKaro.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByGuest_Id(Long guestId);

    List<Booking> findByProperty_Id(Long propertyId);

    List<Booking> findByProperty_IdIn(List<Long> propertyIds);

    List<Booking> findByProperty_Host_Id(Long hostId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByProperty_IdAndStatusAndCheckInBeforeAndCheckOutAfter(
            Long propertyId, BookingStatus status, LocalDate checkOut, LocalDate checkIn);

    List<Booking> findByProperty_IdAndStatusInAndCheckInBeforeAndCheckOutAfter(
            Long propertyId, List<BookingStatus> statuses, LocalDate checkOut, LocalDate checkIn);
}
