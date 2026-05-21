Build a fully functional AirBNB clone prototype using Java Spring Boot with an Admin Panel. The project is already initialized via Spring Initializr. Now implement the following in complete detail:

⚙️ Tech Stack & Dependencies

Spring Boot (latest stable)
Spring Security with JWT authentication
Spring Data JPA + Hibernate
PostgreSQL (or MySQL) as the database
Spring Mail for email notifications
Cloudinary (or AWS S3) for image uploads
Lombok to reduce boilerplate
MapStruct for DTO mapping
Swagger/OpenAPI for API documentation
Thymeleaf for the Admin Panel UI (server-side rendered)


👥 Roles & Authentication

Three roles: GUEST, HOST, ADMIN
JWT-based login and registration
Role-based access control on all endpoints
Endpoints: POST /api/auth/register, POST /api/auth/login, POST /api/auth/logout
Password hashing with BCrypt


🗃️ Database Schema (JPA Entities)
Create the following entities with full relationships:

User — id, name, email, password, phone, profilePicture, role, createdAt, isVerified
Listing — id, title, description, pricePerNight, location, city, country, latitude, longitude, maxGuests, numBedrooms, numBathrooms, amenities (List<String>), images (List<String>), host (User), isApproved, createdAt
Booking — id, guest (User), listing (Listing), checkIn, checkOut, totalPrice, status (PENDING / CONFIRMED / CANCELLED / COMPLETED), createdAt
Review — id, guest (User), listing (Listing), booking (Booking), rating (1–5), comment, createdAt
Payment — id, booking (Booking), amount, paymentMethod, status (PAID / REFUNDED / FAILED), transactionId, paidAt
Wishlist — id, guest (User), listings (List<Listing>)
Notification — id, user (User), message, isRead, createdAt


🔌 REST API Endpoints
Auth

POST /api/auth/register
POST /api/auth/login

Users

GET /api/users/me — get current user profile
PUT /api/users/me — update profile
PUT /api/users/me/password — change password
POST /api/users/me/avatar — upload profile picture

Listings

GET /api/listings — public, paginated, with filters (city, country, priceMin, priceMax, guests, checkIn, checkOut)
GET /api/listings/{id} — listing detail with reviews
POST /api/listings — HOST only, create listing
PUT /api/listings/{id} — HOST only, update own listing
DELETE /api/listings/{id} — HOST only, delete own listing
POST /api/listings/{id}/images — upload images to listing

Bookings

POST /api/bookings — GUEST creates booking (validate date availability, auto-calculate price)
GET /api/bookings/my — GUEST sees their bookings
GET /api/bookings/host — HOST sees bookings for their listings
PUT /api/bookings/{id}/cancel — cancel booking
PUT /api/bookings/{id}/confirm — HOST confirms booking

Reviews

POST /api/listings/{id}/reviews — GUEST posts review (only after COMPLETED booking)
GET /api/listings/{id}/reviews — get all reviews for listing
DELETE /api/reviews/{id} — ADMIN or author deletes review

Wishlist

POST /api/wishlist/{listingId} — add to wishlist
DELETE /api/wishlist/{listingId} — remove from wishlist
GET /api/wishlist — get my wishlist

Payments (Mock)

POST /api/payments/pay/{bookingId} — simulate payment, update booking to CONFIRMED
POST /api/payments/refund/{bookingId} — simulate refund on cancellation

Notifications

GET /api/notifications — get my notifications
PUT /api/notifications/{id}/read — mark as read


🛡️ Admin Panel (Thymeleaf)
Build a server-side rendered admin dashboard at /admin/**, protected by ADMIN role, with the following pages:

Dashboard — total users, listings, bookings, revenue (cards + simple stats)
Users Management — list all users, view profile, ban/unban user, change role
Listings Management — list all listings, approve/reject listing, delete listing
Bookings Management — list all bookings, filter by status, view booking details
Reviews Management — list all reviews, delete inappropriate reviews
Payments Overview — list all payments with status

Use Bootstrap 5 CDN for styling the admin panel.

🔒 Business Logic & Validations

A guest cannot book their own listing
Booking dates must not overlap with existing confirmed bookings
A guest can only review after a COMPLETED booking
Only approved listings appear in public search
Price = nights × pricePerNight (auto-calculated on booking creation)
Send email notification on booking confirmation and cancellation
Pagination on all list endpoints (page, size, sort params)


📁 Package Structure
com.airbnbclone
├── config          → Security, JWT, Cloudinary, Mail config
├── controller      → REST controllers + Admin Thymeleaf controllers
├── service         → Business logic interfaces + implementations
├── repository      → JPA repositories
├── entity          → JPA entities
├── dto             → Request/Response DTOs
├── mapper          → MapStruct mappers
├── exception       → GlobalExceptionHandler, custom exceptions
└── util            → JWT utility, helpers

📄 Additional Requirements

Implement a GlobalExceptionHandler with @RestControllerAdvice returning structured error responses
All endpoints must return a standard API response wrapper: { success, message, data, timestamp }
Add Swagger UI at /swagger-ui.html documenting all endpoints
Add data.sql or DataLoader with seed data (1 admin, 2 hosts, 3 guests, 5 listings, sample bookings)
Write application.yml with clearly commented configuration sections for DB, JWT, mail, and Cloudinary


Generate all files completely — entities, DTOs, services, controllers, repositories, config classes, Thymeleaf templates, and application.yml. Do not leave any TODO or placeholder — every method must be fully implemented.


