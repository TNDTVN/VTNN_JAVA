package com.example.vtnn.service;

import com.example.vtnn.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalOrders", orderRepository.count());
        stats.put("totalSuppliers", supplierRepository.count());
        stats.put("totalProducts", productRepository.count());
        stats.put("totalCustomers", customerRepository.count());
        return stats;
    }

    public List<Object[]> getRecentNotifications(int receiverID) {
        return notificationRepository.findTop5ByReceiverIDOrReceiverIDIsNullOrderByCreatedDateDesc(receiverID)
                .stream()
                .map(n -> new Object[]{n.getTitle(), n.getContent(), n.getCreatedDate(), n.isRead()})
                .toList();
    }

    public List<Object[]> getApprovedOrders(int year, int month) {
        List<Object[]> orders = orderRepository.findApprovedOrdersWithEmployeeDetails(year, month);
        return orders != null ? orders : Collections.emptyList();
    }

    public List<Object[]> getRevenueByMonth() {
        List<Object[]> revenue = orderDetailRepository.findRevenueByMonth();
        return revenue != null ? revenue : Collections.emptyList();
    }
}