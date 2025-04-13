package com.example.vtnn.repository;

import com.example.vtnn.model.Account;
import com.example.vtnn.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByEmail(String email);
    Page<Employee> findEmployeesByFirstNameContainingIgnoreCase(String firstname, Pageable pageable);
    List<Employee> findAll();

    @Query("SELECT e FROM Employee e WHERE LOWER(e.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Employee> findByFirstNameOrLastNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Employee> findByAccountID(Integer accountID);
    Optional<Employee> findByAccountAccountID(int accountID);
}