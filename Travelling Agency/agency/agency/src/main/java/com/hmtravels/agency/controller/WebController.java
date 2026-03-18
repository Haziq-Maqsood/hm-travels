package com.hmtravels.agency.controller;

import com.hmtravels.agency.dto.TripRequest;
import com.hmtravels.agency.model.Customer;
import com.hmtravels.agency.model.Reservation;
import com.hmtravels.agency.model.Route;
import com.hmtravels.agency.model.Trip;
import com.hmtravels.agency.model.Vehicle;
import com.hmtravels.agency.repository.CustomerRepository;
import com.hmtravels.agency.repository.ReservationRepository;
import com.hmtravels.agency.repository.RouteRepository;
import com.hmtravels.agency.repository.TripRepository;
import com.hmtravels.agency.repository.VehicleRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
public class WebController {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // ==========================================
    // LOGIN PAGE
    // ==========================================
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // ==========================================
    // DASHBOARD
    // ==========================================
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        long totalRoutes = routeRepository.count();
        long totalVehicles = vehicleRepository.count();
        long totalBookings = reservationRepository.count();

        model.addAttribute("title", "HM Travels - Agency Dashboard");
        model.addAttribute("totalRoutes", totalRoutes);
        model.addAttribute("totalVehicles", totalVehicles);
        model.addAttribute("totalBookings", totalBookings);

        return "dashboard";
    }

    // ==========================================
    // ROUTES
    // ==========================================
    @GetMapping("/routes")
    public String manageRoutes(Model model) {
        model.addAttribute("title", "Manage Routes - HM Travels");
        model.addAttribute("routes", routeRepository.findAll());
        return "routes";
    }

    @PostMapping("/routes/add")
    public String addRouteWeb(@ModelAttribute Route route, RedirectAttributes redirectAttributes) {
        try {
            routeRepository.save(route);
            redirectAttributes.addFlashAttribute("success", "Route added successfully!");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // This catches the 'unique = true' violation
            redirectAttributes.addFlashAttribute("error", "Error: A route with the name '" + route.getRouteName() + "' already exists!");
        }
        return "redirect:/routes";
    }
    // ==========================================
    // VEHICLES
    // ==========================================
    @GetMapping("/vehicles")
    public String manageVehicles(Model model) {
        model.addAttribute("title", "Fleet Management - HM Travels");
        model.addAttribute("vehicles", vehicleRepository.findAll());
        return "vehicles";
    }

    @PostMapping("/vehicles/add")
    public String addVehicleWeb(@ModelAttribute Vehicle vehicle) {
        vehicleRepository.save(vehicle);
        return "redirect:/vehicles";
    }

    // ==========================================
    // TRIPS
    // ==========================================
    @GetMapping("/trips")
    public String manageTrips(Model model) {

        model.addAttribute("title", "Schedule Trips - HM Travels");
        model.addAttribute("trips", tripRepository.findAll());
        model.addAttribute("routes", routeRepository.findAll());
        model.addAttribute("vehicles", vehicleRepository.findAll());

        return "trips";
    }

    @PostMapping("/trips/add")
    public String addTripWeb(@ModelAttribute TripRequest request) {

        Optional<Route> route = routeRepository.findById(request.getRouteId());
        Optional<Vehicle> vehicle = vehicleRepository.findById(request.getVehicleId());

        if (route.isPresent() && vehicle.isPresent()) {

            Trip trip = new Trip();
            trip.setRoute(route.get());
            trip.setVehicle(vehicle.get());
            trip.setDepartureTime(request.getDepartureTime());
            trip.setDriverName(request.getDriverName());

            tripRepository.save(trip);
        }

        return "redirect:/trips";
    }

    // ==========================================
    // CUSTOMERS
    // ==========================================
    @GetMapping("/customers")
    public String manageCustomers(Model model) {
        model.addAttribute("title", "Customer Management - HM Travels");
        model.addAttribute("customers", customerRepository.findAll());
        return "customers";
    }

    @PostMapping("/customers/add")
    public String addCustomerWeb(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {

        if (customerRepository.findByCnic(customer.getCnic()).isPresent()) {

            redirectAttributes.addFlashAttribute("error", "CNIC already exists!");
            return "redirect:/customers";
        }

        customerRepository.save(customer);

        redirectAttributes.addFlashAttribute("success", "Customer registered successfully!");

        return "redirect:/customers";
    }

    // ==========================================
    // BOOKINGS
    // ==========================================
    // ==========================================
    // 2. VIEW BOOKINGS (TABS & DEFAULT TO TODAY)
    // ==========================================
    @GetMapping("/bookings")
    public String viewBookings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate filterDate,
            @RequestParam(required = false, defaultValue = "false") boolean viewAll,
            Model model) {

        List<Reservation> bookings;

        if (viewAll) {
            // Show absolute entire history
            bookings = reservationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            model.addAttribute("selectedDate", null);
        } else {
            // Default to TODAY if no date is picked
            if (filterDate == null) {
                filterDate = LocalDate.now();
            }
            LocalDateTime startOfDay = filterDate.atStartOfDay();
            LocalDateTime endOfDay = filterDate.atTime(LocalTime.MAX);
            bookings = reservationRepository.findByTripDepartureTimeBetween(startOfDay, endOfDay);
            model.addAttribute("selectedDate", filterDate);
        }

        model.addAttribute("title", "Booking Management - HM Travels");
        model.addAttribute("bookings", bookings);
        model.addAttribute("trips", tripRepository.findByDepartureTimeAfter(LocalDateTime.now()));        model.addAttribute("routes", routeRepository.findAll());

        return "bookings";
    }

    // ==========================================
    // ADMIN BOOK TICKET
    // ==========================================
    @PostMapping("/bookings/add")
    public String adminBookTicket(
            @RequestParam String cnic,
            @RequestParam Long tripId,
            @RequestParam Integer seatNo,
            RedirectAttributes redirectAttributes) {

        Optional<Customer> customerOpt = customerRepository.findByCnic(cnic);

        if (customerOpt.isEmpty()) {

            redirectAttributes.addFlashAttribute("error",
                    "Customer with CNIC " + cnic + " not found.");

            return "redirect:/bookings";
        }

        Optional<Trip> tripOpt = tripRepository.findById(tripId);

        if (tripOpt.isEmpty()) {
            return "redirect:/bookings";
        }

        Trip trip = tripOpt.get();

        if (seatNo <= 0 || seatNo > trip.getVehicle().getCapacity()) {

            redirectAttributes.addFlashAttribute("error",
                    "Invalid seat number. Capacity: " +
                            trip.getVehicle().getCapacity());

            return "redirect:/bookings";
        }

        if (reservationRepository.existsByTripIdAndSeatNo(tripId, seatNo)) {

            redirectAttributes.addFlashAttribute("error",
                    "Seat " + seatNo + " already booked!");

            return "redirect:/bookings";
        }

        BigDecimal baseFare = trip.getRoute().getBaseFare();

        Reservation res = new Reservation();
        res.setCustomer(customerOpt.get());
        res.setTrip(trip);
        res.setSeatNo(seatNo);
        res.setTotalFare(baseFare);
        res.setBookingStatus("CONFIRMED");

        reservationRepository.save(res);

        redirectAttributes.addFlashAttribute("success",
                "Ticket booked successfully!");

        return "redirect:/bookings";
    }


    @GetMapping("/bookings/ticket/{id}")
    public String showTicket(@PathVariable Long id, org.springframework.ui.Model model) {
        // Fetch the reservation by ID (assuming you have reservationRepository autowired)
        java.util.Optional<Reservation> reservation = reservationRepository.findById(id);

        if (reservation.isPresent()) {
            model.addAttribute("booking", reservation.get());
            return "ticket"; // This tells Spring to look for ticket.html
        }

        // If someone types a wrong ID, send them back to bookings
        return "redirect:/bookings?error=Ticket+not+found";
    }
}