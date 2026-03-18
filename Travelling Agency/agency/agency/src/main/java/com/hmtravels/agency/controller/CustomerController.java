package com.hmtravels.agency.controller;

import com.hmtravels.agency.model.Customer;
import com.hmtravels.agency.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    // 1. GET Method - List all customers
    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // 2. POST Method - Register a new customer
    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@RequestBody Customer customer) {
        if (customerRepository.findByCnic(customer.getCnic()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: CNIC is already registered!");
        }
        if (customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email address is already registered!");
        }
        try {
            // Save the customer and return the saved object as JSON
            Customer savedCustomer = customerRepository.save(customer);
            return ResponseEntity.ok(savedCustomer);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: A database conflict occurred. Please check all fields.");
        }
    }

    // 3. POST Method - Login using CNIC and Password
    @PostMapping("/login")
    public ResponseEntity<?> loginCustomer(@RequestBody Customer loginRequest) {
        Optional<Customer> customer = customerRepository.findByCnic(loginRequest.getCnic());

        if (customer.isPresent() && customer.get().getPassword().equals(loginRequest.getPassword())) {
            // Return the customer object as JSON so Android knows WHO logged in
            return ResponseEntity.ok(customer.get());
        }
        return ResponseEntity.status(401).body("Error: Invalid CNIC or password.");
    }
}