package com.example.vtnn.controller;

import com.example.vtnn.model.Notification;
import com.example.vtnn.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    // Gửi thông báo (chỉ admin)
    @PostMapping(value = "/send", consumes = "application/json")
    public ResponseEntity<String> sendNotification(@RequestBody Notification notification) {
        notificationService.sendNotification(notification);
        return ResponseEntity.ok("Notification sent successfully");
    }

    // Lấy thông báo cho người dùng
    @GetMapping("/user")
    public ResponseEntity<Page<Notification>> getNotificationsForUser(
            @RequestParam Integer accountID,
            @RequestParam String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getNotificationsForUser(accountID, role, pageable);
        return ResponseEntity.ok(notifications);
    }

    // Đánh dấu thông báo là đã đọc
    @PutMapping("/mark-as-read/{notificationID}")
    public ResponseEntity<String> markAsRead(@PathVariable Integer notificationID) {
        notificationService.markAsRead(notificationID);
        return ResponseEntity.ok("Notification marked as read");
    }


}