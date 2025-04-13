package com.example.vtnn.repository;

import com.example.vtnn.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findTop5ByOrderByCreatedDateDesc();
    List<Notification> findTop5ByReceiverIDOrReceiverIDIsNullOrderByCreatedDateDesc(int receiverID);
    List<Notification> findByReceiverID(Integer receiverID);

    List<Notification> findByReceiverIDIsNull();

    Page<Notification> findByReceiverIDOrReceiverIDIsNull(Integer receiverID, Pageable pageable);
    Page<Notification> findBySenderID(Integer senderID, Pageable pageable);
}
