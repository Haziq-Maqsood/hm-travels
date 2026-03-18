package com.hmtravels.agency.repository;

import com.hmtravels.agency.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    // Spring Boot automatically writes the SQL query for this just based on the method name!
    List<Trip> findByRouteId(Long routeId);
    // NEW: Automatically finds trips where the departure time is in the future
    List<Trip> findByDepartureTimeAfter(LocalDateTime time);
}