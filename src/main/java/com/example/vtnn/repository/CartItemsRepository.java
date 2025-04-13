package com.example.vtnn.repository;

import com.example.vtnn.model.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemsRepository extends JpaRepository<CartItems, Integer> {
    List<CartItems> findByCustomerID(int customerID);
    Optional<CartItems> findByCustomerIDAndProductID(int customerID, int productID);
    void deleteByCustomerID(int customerID);
}