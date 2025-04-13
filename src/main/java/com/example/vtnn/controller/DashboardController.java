package com.example.vtnn.controller;

import com.example.vtnn.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        try {
            return ResponseEntity.ok(dashboardService.getStats());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Object[]>> getRecentNotifications(@RequestParam("receiverID") int receiverID) {
        try {
            return ResponseEntity.ok(dashboardService.getRecentNotifications(receiverID));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    @GetMapping("/approved-orders")
    public ResponseEntity<List<Object[]>> getApprovedOrders(
            @RequestParam(value = "year", defaultValue = "-1") int year,
            @RequestParam(value = "month", defaultValue = "-1") int month
    ) {
        try {
            List<Object[]> orders = dashboardService.getApprovedOrders(year, month);
            return ResponseEntity.ok(orders != null ? orders : Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    @GetMapping("/revenue")
    public ResponseEntity<List<Object[]>> getRevenueByMonth() {
        try {
            return ResponseEntity.ok(dashboardService.getRevenueByMonth());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }
}