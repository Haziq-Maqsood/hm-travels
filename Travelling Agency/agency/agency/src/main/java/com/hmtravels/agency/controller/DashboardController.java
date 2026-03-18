package com.hmtravels.agency.controller;

import com.hmtravels.agency.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*") // Allows your frontend to access it
public class DashboardController {

    @Autowired private RouteRepository routeRepo;
    @Autowired private VehicleRepository vehicleRepo;
    @Autowired private TripRepository tripRepo;

    @GetMapping("/sync")
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        // Pack everything into one single response
        data.put("routes", routeRepo.findAll());
        data.put("vehicles", vehicleRepo.findAll());
        data.put("trips", tripRepo.findAll());
        return data;
    }
}