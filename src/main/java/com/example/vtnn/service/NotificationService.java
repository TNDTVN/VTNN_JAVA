package com.example.vtnn.service;

import com.example.vtnn.model.Account;
import com.example.vtnn.model.Customer;
import com.example.vtnn.model.Employee;
import com.example.vtnn.model.Notification;
import com.example.vtnn.repository.AccountRepository;
import com.example.vtnn.repository.CustomerRepository;
import com.example.vtnn.repository.EmployeeRepository;
import com.example.vtnn.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CustomerRepository customerRepository;

    // Gửi thông báo
    public void sendNotification(Notification notification) {
        notification.setCreatedDate(new Date());
        notification.setRead(false);
        // Lưu thông báo trực tiếp, không cần tạo nhiều bản ghi nếu receiverID là null
        notificationRepository.save(notification);
    }

    public void markAsRead(Integer notificationID) {
        Notification notification = notificationRepository.findById(notificationID)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        System.out.println("Before update: notificationID=" + notificationID + ", isRead=" + notification.isRead());
        notification.setRead(true);
        Notification savedNotification = notificationRepository.save(notification);
        System.out.println("After update: notificationID=" + notificationID + ", isRead=" + savedNotification.isRead());
    }

    public Page<Notification> getNotificationsForUser(Integer accountID, String role, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("createdDate").descending()
        );
        Page<Notification> notifications;
        if ("CUSTOMER".equals(role)) {
            notifications = notificationRepository.findByReceiverIDOrReceiverIDIsNull(accountID, sortedPageable);
        } else if ("ADMIN".equals(role)) {
            // Admin thấy tất cả thông báo
            notifications = notificationRepository.findAll(sortedPageable);
        } else if ("EMPLOYEE".equals(role)) {
            // Employee chỉ thấy thông báo họ gửi
            notifications = notificationRepository.findBySenderID(accountID, sortedPageable);
        } else {
            return Page.empty();
        }

        List<Notification> enhancedNotifications = new ArrayList<>();
        for (Notification notification : notifications.getContent()) {
            Optional<Employee> sender = employeeRepository.findByAccountID(notification.getSenderID());
            if (sender.isPresent()) {
                notification.setSenderName(sender.get().getFirstName() + " " + sender.get().getLastName());
            }
            if (notification.getReceiverID() != null) {
                Optional<Customer> receiver = customerRepository.findByAccountID(notification.getReceiverID());
                receiver.ifPresent(customer -> notification.setReceiverName(customer.getCustomerName()));
            } else {
                notification.setReceiverName("Tất cả khách hàng");
            }
            enhancedNotifications.add(notification);
        }
        System.out.println("Returning notifications: " + enhancedNotifications);
        return new PageImpl<>(enhancedNotifications, sortedPageable, notifications.getTotalElements());
    }
}