package com.hmtravels.agency.controller;

import com.hmtravels.agency.dto.TripRequest;
import com.hmtravels.agency.model.Route;
import com.hmtravels.agency.model.Trip;
import com.hmtravels.agency.model.Vehicle;
import com.hmtravels.agency.repository.RouteRepository;
import com.hmtravels.agency.repository.TripRepository;
import com.hmtravels.agency.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private VehicleRepository vehicleRepository;
    // 1. GET All Trips (For the Web Portal Dashboard)
    @GetMapping
    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    // 2. GET Trips by Route ID (For the Mobile App Search Screen)
    @GetMapping("/search/{routeId}")
    public ResponseEntity<?> getTripsByRoute(@PathVariable Long routeId) {
        List<Trip> trips = tripRepository.findByRouteId(routeId);

        if (trips.isEmpty()) {
            return ResponseEntity.status(404).body("No trips found for this route.");
        }
        return ResponseEntity.ok(trips);
    }

    @PostMapping("/schedule")
    public ResponseEntity<String> scheduleTrip(@RequestBody TripRequest request) {
        Optional<Route> route = routeRepository.findById(request.getRouteId());
        Optional<Vehicle> vehicle = vehicleRepository.findById(request.getVehicleId());

        if (route.isEmpty() || vehicle.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Invalid Route or Vehicle ID.");
        }

        Trip trip = new Trip();
        trip.setRoute(route.get());
        trip.setVehicle(vehicle.get());
        trip.setDepartureTime(request.getDepartureTime());
        trip.setDriverName(request.getDriverName());

        tripRepository.save(trip);
        return ResponseEntity.ok("Trip scheduled successfully with ID: " + trip.getId());
    }
}