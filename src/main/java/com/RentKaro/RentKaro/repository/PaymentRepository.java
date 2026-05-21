package com.RentKaro.RentKaro.repository;

import com.RentKaro.RentKaro.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBooking_Id(Long bookingId);

    List<Payment> findByStatus(com.RentKaro.RentKaro.model.PaymentStatus status);
}
