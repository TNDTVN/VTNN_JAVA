package com.example.vtnn.service;

import com.example.vtnn.model.OrderDetail;
import com.example.vtnn.repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDetailService {
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public List<OrderDetail> getOrderDetailsByOrderId(int orderID) {
        return orderDetailRepository.findAll().stream()
                .filter(detail -> detail.getOrderID() == orderID)
                .toList();
    }
}