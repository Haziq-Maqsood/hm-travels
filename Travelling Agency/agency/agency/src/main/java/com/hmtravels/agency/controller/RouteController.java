package com.hmtravels.agency.controller;

import com.hmtravels.agency.model.Route;
import com.hmtravels.agency.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private RouteRepository routeRepository;

    @GetMapping
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    @PostMapping("/add")
    public ResponseEntity<String> addRoute(@RequestBody Route route) {
        routeRepository.save(route);
        return ResponseEntity.ok("Route added successfully!");
    }
}