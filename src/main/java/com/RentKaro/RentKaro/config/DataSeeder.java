package com.RentKaro.RentKaro.config;

import com.RentKaro.RentKaro.model.*;
import com.RentKaro.RentKaro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            if (userRepository.findByEmail("admin@rentkaro.com").isEmpty()) {
                log.info("Admin account not found. Creating default admin...");
                userRepository.save(User.builder()
                        .name("Admin User")
                        .email("admin@rentkaro.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .isVerified(true)
                        .phone("+91-9000000001")
                        .build());
                log.info("Admin account created successfully (admin@rentkaro.com / admin123)");
            }

            if (userRepository.count() > 1) {
                log.info("Database already seeded with sample data. Skipping full seeder.");
                return;
            }
        } catch (Exception e) {
            log.warn("MongoDB not available. Skipping data seeding. Start MongoDB and restart the app to seed data.");
            return;
        }

        log.info("Seeding database with sample properties, guests, and hosts...");

        // ─── 2 Hosts ───
        User host1 = userRepository.save(User.builder()
                .name("Rahul Sharma")
                .email("rahul@rentkaro.com")
                .password(passwordEncoder.encode("host123"))
                .role(Role.HOST)
                .isVerified(true)
                .phone("+91-9000000002")
                .build());

        User host2 = userRepository.save(User.builder()
                .name("Priya Patel")
                .email("priya@rentkaro.com")
                .password(passwordEncoder.encode("host123"))
                .role(Role.HOST)
                .isVerified(true)
                .phone("+91-9000000003")
                .build());

        // ─── 3 Guests ───
        User guest1 = userRepository.save(User.builder()
                .name("Arjun Mehta")
                .email("arjun@rentkaro.com")
                .password(passwordEncoder.encode("guest123"))
                .role(Role.GUEST)
                .isVerified(true)
                .phone("+91-9000000004")
                .build());

        User guest2 = userRepository.save(User.builder()
                .name("Sneha Gupta")
                .email("sneha@rentkaro.com")
                .password(passwordEncoder.encode("guest123"))
                .role(Role.GUEST)
                .isVerified(true)
                .phone("+91-9000000005")
                .build());

        User guest3 = userRepository.save(User.builder()
                .name("Vikram Singh")
                .email("vikram@rentkaro.com")
                .password(passwordEncoder.encode("guest123"))
                .role(Role.GUEST)
                .isVerified(true)
                .phone("+91-9000000006")
                .build());

        // ─── 5 Listings ───
        Property l1 = propertyRepository.save(Property.builder()
                .title("Luxury Beachfront Villa in Goa")
                .description("A stunning 4-bedroom villa with private beach access, infinity pool, and panoramic sea views. Perfect for families and groups looking for a premium getaway.")
                .pricePerNight(8500.0)
                .location("Calangute Beach Road, North Goa")
                .city("Goa")
                .country("India")
                .latitude(15.5449)
                .longitude(73.7551)
                .maxGuests(8)
                .numBedrooms(4)
                .numBathrooms(3)
                .amenities(Arrays.asList("WiFi", "Pool", "AC", "Kitchen", "Parking", "Beach Access", "BBQ Grill"))
                .images(Arrays.asList("https://images.unsplash.com/photo-1613490493576-7fde63acd811?w=800", "https://images.unsplash.com/photo-1582268611958-ebfd161ef9cf?w=800"))
                .host(host1)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build());

        Property l2 = propertyRepository.save(Property.builder()
                .title("Cozy Mountain Cottage in Manali")
                .description("A charming wooden cottage nestled in the Himalayas with mountain views, fireplace, and apple orchard. Ideal for couples and solo travelers seeking tranquility.")
                .pricePerNight(3200.0)
                .location("Old Manali Road, Near Manu Temple")
                .city("Manali")
                .country("India")
                .latitude(32.2432)
                .longitude(77.1892)
                .maxGuests(4)
                .numBedrooms(2)
                .numBathrooms(1)
                .amenities(Arrays.asList("WiFi", "Fireplace", "Kitchen", "Mountain View", "Heating", "Garden"))
                .images(Arrays.asList("https://images.unsplash.com/photo-1518780664697-55e3ad937233?w=800", "https://images.unsplash.com/photo-1449824913935-59a10b8d2000?w=800"))
                .host(host1)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build());

        Property l3 = propertyRepository.save(Property.builder()
                .title("Modern Apartment in South Mumbai")
                .description("A sleek 2BHK apartment in the heart of Mumbai with city skyline views, modern amenities, and walking distance to Marine Drive and Gateway of India.")
                .pricePerNight(5500.0)
                .location("Colaba, South Mumbai")
                .city("Mumbai")
                .country("India")
                .latitude(18.9067)
                .longitude(72.8147)
                .maxGuests(4)
                .numBedrooms(2)
                .numBathrooms(2)
                .amenities(Arrays.asList("WiFi", "AC", "Gym", "Elevator", "City View", "24/7 Security"))
                .images(Arrays.asList("https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800", "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800"))
                .host(host2)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build());

        Property l4 = propertyRepository.save(Property.builder()
                .title("Heritage Haveli in Jaipur")
                .description("Experience royal Rajasthani hospitality in this restored 200-year-old haveli with intricate architecture, courtyard, and rooftop dining with fort views.")
                .pricePerNight(4800.0)
                .location("Near Hawa Mahal, Pink City")
                .city("Jaipur")
                .country("India")
                .latitude(26.9239)
                .longitude(75.8267)
                .maxGuests(6)
                .numBedrooms(3)
                .numBathrooms(2)
                .amenities(Arrays.asList("WiFi", "AC", "Courtyard", "Rooftop", "Heritage", "Breakfast Included"))
                .images(Arrays.asList("https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800", "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800"))
                .host(host2)
                .approvalStatus(ApprovalStatus.APPROVED)
                .build());

        Property l5 = propertyRepository.save(Property.builder()
                .title("Treehouse Retreat in Wayanad")
                .description("Unique treehouse experience surrounded by lush tropical forest in Kerala's Wayanad. Wake up to birdsong and enjoy nature walks, waterfalls nearby.")
                .pricePerNight(2800.0)
                .location("Vythiri, Wayanad")
                .city("Wayanad")
                .country("India")
                .latitude(11.5687)
                .longitude(76.0583)
                .maxGuests(2)
                .numBedrooms(1)
                .numBathrooms(1)
                .amenities(Arrays.asList("WiFi", "Nature View", "Breakfast Included", "Guided Walks", "Eco-Friendly"))
                .images(Arrays.asList("https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=800", "https://images.unsplash.com/photo-1571003123894-1f0594d2b5d9?w=800"))
                .host(host1)
                .approvalStatus(ApprovalStatus.PENDING)
                .build());

        // ─── Sample Bookings ───
        Booking b1 = bookingRepository.save(Booking.builder()
                .guest(guest1)
                .property(l1)
                .checkIn(LocalDate.now().minusDays(10))
                .checkOut(LocalDate.now().minusDays(5))
                .totalPrice(5 * l1.getPricePerNight())
                .status(BookingStatus.COMPLETED)
                .build());

        Booking b2 = bookingRepository.save(Booking.builder()
                .guest(guest2)
                .property(l2)
                .checkIn(LocalDate.now().plusDays(5))
                .checkOut(LocalDate.now().plusDays(10))
                .totalPrice(5 * l2.getPricePerNight())
                .status(BookingStatus.CONFIRMED)
                .build());

        Booking b3 = bookingRepository.save(Booking.builder()
                .guest(guest3)
                .property(l3)
                .checkIn(LocalDate.now().plusDays(2))
                .checkOut(LocalDate.now().plusDays(4))
                .totalPrice(2 * l3.getPricePerNight())
                .status(BookingStatus.PENDING)
                .build());

        // ─── Sample Reviews (for completed booking) ───
        reviewRepository.save(Review.builder()
                .guest(guest1)
                .property(l1)
                .booking(b1)
                .rating(5)
                .comment("Absolutely stunning property! The beach access was amazing and the villa was spotless. Will definitely come back!")
                .build());

        // ─── Sample Payments ───
        paymentRepository.save(Payment.builder()
                .booking(b1)
                .amount(b1.getTotalPrice())
                .paymentMethod("MOCK_CARD")
                .status(PaymentStatus.PAID)
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paidAt(LocalDateTime.now().minusDays(10))
                .build());

        paymentRepository.save(Payment.builder()
                .booking(b2)
                .amount(b2.getTotalPrice())
                .paymentMethod("MOCK_UPI")
                .status(PaymentStatus.PAID)
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .paidAt(LocalDateTime.now().minusDays(2))
                .build());

        log.info("Database seeded successfully!");
        log.info("Admin login: admin@rentkaro.com / admin123");
        log.info("Host login: rahul@rentkaro.com / host123");
        log.info("Guest login: arjun@rentkaro.com / guest123");
    }
}
