package com.RentKaro.RentKaro.repository;

import com.RentKaro.RentKaro.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Optional<Wishlist> findByGuest_IdAndProperty_Id(Long guestId, Long propertyId);

    List<Wishlist> findByGuest_Id(Long guestId);

    void deleteByGuest_IdAndProperty_Id(Long guestId, Long propertyId);
}
