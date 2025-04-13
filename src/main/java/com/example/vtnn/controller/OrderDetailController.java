package com.example.vtnn.controller;

import com.example.vtnn.model.OrderDetail;
import com.example.vtnn.service.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orderdetails")
public class OrderDetailController {
    @Autowired
    private OrderDetailService orderDetailService;

    @GetMapping
    public List<OrderDetail> getOrderDetailsByOrderId(@RequestParam("orderID") int orderID) {
        return orderDetailService.getOrderDetailsByOrderId(orderID);
    }
}