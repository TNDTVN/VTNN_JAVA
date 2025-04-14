package com.example.vtnn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int notificationID;
    @Column(name = "title", length = 1000)
    private String title;

    @Column(name = "content", length = 4000)
    private String content;

    @Column(name = "senderid")
    private int senderID;

    @Column(name = "receiverid", nullable = true)
    private Integer receiverID;

    @Column(name = "createddate")
    private Date createdDate;

    @Column(name = "isread")
    private boolean isRead;

    public Notification() {
    }

    public int getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(int notificationID) {
        this.notificationID = notificationID;
    }
    public Notification(String title, String content, int senderID, int receiverID, Date createdDate, boolean isRead) {
        this.title = title;
        this.content = content;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.createdDate = createdDate;
        this.isRead = isRead;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(Integer receiverID) {
        this.receiverID = receiverID;
    }

    public int getSenderID() {
        return senderID;
    }

    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }


    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }



    @Transient // Không lưu vào DB, chỉ dùng để trả về frontend
    private String senderName;

    @Transient // Không lưu vào DB, chỉ dùng để trả về frontend
    private String receiverName;

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
}

