package com.example.vtnn.repository;

import com.example.vtnn.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    @Query("SELECT MONTH(o.orderDate), SUM(od.unitPrice * od.quantity * (1 - od.discount)) " +
            "FROM OrderDetail od " +
            "JOIN od.order o " +
            "WHERE o.isCancelled = false " +
            "GROUP BY MONTH(o.orderDate)")
    List<Object[]> findRevenueByMonthNotCancelled();
}
