package com.RentKaro.RentKaro.repository;

import com.RentKaro.RentKaro.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByBookingId(String bookingId);

    List<Payment> findByStatus(com.RentKaro.RentKaro.model.PaymentStatus status);
}
