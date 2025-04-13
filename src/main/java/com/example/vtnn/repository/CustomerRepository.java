package com.example.vtnn.repository;

import com.example.vtnn.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    List<Customer> findByAccountAccountID(int accountID);
    Optional<Customer> findByEmail(String email);
    Page<Customer> findCustomersByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);
    List<Customer> findAll();

    @Query("SELECT c FROM Customer c WHERE LOWER(c.customerName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(c.contactName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Customer> findByCustomerNameOrContactNameContainingIgnoreCase(String name, Pageable pageable);

    Optional<Customer> findByAccountID(Integer accountID);
}