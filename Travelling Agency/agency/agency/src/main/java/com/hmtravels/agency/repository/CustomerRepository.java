package com.hmtravels.agency.repository;

import com.hmtravels.agency.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Custom method to find a user by their CNIC for login
    Optional<Customer> findByCnic(String cnic);
    Optional<Customer> findByEmail(String email);
    // Add this exact line
    Optional<Customer> findByCnicAndPassword(String cnic, String password);
}