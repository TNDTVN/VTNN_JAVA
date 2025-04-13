package com.example.vtnn.repository;

import com.example.vtnn.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByUsername(String username);
    Page<Account> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    Optional<Account> findByTokenCode(String tokenCode);
    List<Account> findAll();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<Account> findByRole(String role);
}