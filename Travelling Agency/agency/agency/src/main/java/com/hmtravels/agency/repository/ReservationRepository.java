package com.hmtravels.agency.repository;

import com.hmtravels.agency.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByTripIdAndSeatNo(Long tripId, Integer seatNo);

    // Finds all bookings between the start and end of a specific day
    List<Reservation> findByTripDepartureTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Reservation> findByTripId(Long tripId);
    List<Reservation> findByCustomerId(Long customerId);
}