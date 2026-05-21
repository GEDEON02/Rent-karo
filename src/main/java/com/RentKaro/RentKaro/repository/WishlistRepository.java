package com.RentKaro.RentKaro.repository;

import com.RentKaro.RentKaro.model.Wishlist;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends MongoRepository<Wishlist, String> {

    Optional<Wishlist> findByGuestId(String guestId);
}
