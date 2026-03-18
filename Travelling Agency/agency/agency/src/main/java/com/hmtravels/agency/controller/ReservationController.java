package com.hmtravels.agency.controller;

import com.hmtravels.agency.dto.ReservationRequest;
import com.hmtravels.agency.model.Customer;
import com.hmtravels.agency.model.Reservation;
import com.hmtravels.agency.model.Trip;
import com.hmtravels.agency.repository.CustomerRepository;
import com.hmtravels.agency.repository.ReservationRepository;
import com.hmtravels.agency.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TripRepository tripRepository;

    @PostMapping("/book")
    public ResponseEntity<?> bookTicket(@RequestBody ReservationRequest request) { // 1. Changed <String> to <?>
        Optional<Customer> customer = customerRepository.findById(request.getCustomerId());
        Optional<Trip> trip = tripRepository.findById(request.getTripId());

        if (customer.isEmpty() || trip.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Invalid Customer or Trip ID.");
        }

        // 1. Capacity Check
        if (request.getSeatNo() <= 0 || request.getSeatNo() > trip.get().getVehicle().getCapacity()) {
            return ResponseEntity.badRequest().body("Error: Seat number is out of bounds for this vehicle.");
        }

        // 2. Early existence check (Optional, but gives a cleaner error message)
        if (reservationRepository.existsByTripIdAndSeatNo(request.getTripId(), request.getSeatNo())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: Seat " + request.getSeatNo() + " is already booked!");
        }

        // 3. Calculate Fare based on your HM Travels Console App logic
        BigDecimal baseFare = trip.get().getRoute().getBaseFare();
        String vehicleType = trip.get().getVehicle().getVehicleType();
        BigDecimal fareMultiplier = BigDecimal.ONE; // Default for Bus

        if ("AEROPLANE".equalsIgnoreCase(vehicleType)) {
            fareMultiplier = BigDecimal.valueOf(70);
        } else if ("CAR".equalsIgnoreCase(vehicleType)) {
            fareMultiplier = BigDecimal.valueOf(12);
        }

        BigDecimal totalFare = baseFare.multiply(fareMultiplier);

        // 4. Create Reservation
        Reservation reservation = new Reservation();
        reservation.setCustomer(customer.get());
        reservation.setTrip(trip.get());
        reservation.setSeatNo(request.getSeatNo());
        // Assuming your Reservation entity takes a Double or BigDecimal. Adjust if necessary!
        reservation.setTotalFare(totalFare);
        reservation.setBookingStatus("CONFIRMED");

        // 5. The Double-Booking Catch Block
        try {
            // 2. Capture the saved object from the database
            Reservation savedReservation = reservationRepository.save(reservation);

            // 3. THE FIX: Return the saved object as JSON!
            return ResponseEntity.ok(savedReservation);

        } catch (DataIntegrityViolationException e) {
            // If two users hit 'save' at the exact same millisecond, the database throws this error.
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Someone just snagged this seat. Please choose another!");
        }
    }
    @GetMapping
    public List<Reservation> getAllReservations() {
        // THE FIX: This tells the database to hand you the list backward (LIFO)
        return reservationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<List<Reservation>> getReservationsByTrip(@PathVariable Long tripId) {
        return ResponseEntity.ok(reservationRepository.findByTripId(tripId));
    }
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Reservation>> getCustomerBookings(@PathVariable Long customerId) {
        List<Reservation> bookings = reservationRepository.findByCustomerId(customerId);
        return ResponseEntity.ok(bookings);
    }

    @DeleteMapping("/cancel/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        if (reservationRepository.existsById(id)) {
            reservationRepository.deleteById(id);
            // Returning .build() sends an empty, successful 200 OK response.
            // This prevents any JSON parsing crashes on Android!
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Error: Reservation not found.");
    }


}