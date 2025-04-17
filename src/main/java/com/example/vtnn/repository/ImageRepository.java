package com.example.vtnn.repository;

import com.example.vtnn.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {
    Optional<Image> findByProductID(int productID);
    List<Image> findAllByProductID(int id);
    @Modifying
    @Query("DELETE FROM Image i WHERE i.productID = :productID")
    void deleteAllByProductID(int productID);
}
