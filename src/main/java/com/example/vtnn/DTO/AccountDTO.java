package com.example.vtnn.DTO;

import com.example.vtnn.model.Account;

public class AccountDTO {
    private int accountID;
    private String username;
    private String email;
    private String role;
    private boolean isLocked;
    private String profileImage; // Thêm trường này

    public AccountDTO(Account account) {
        this.accountID = account.getAccountID();
        this.username = account.getUsername();
        this.email = account.getEmail();
        this.role = account.getRole();
        this.isLocked = account.isLocked();
        this.profileImage = account.getProfileImage(); // Ánh xạ profileImage
    }

    // Getter và Setter cho profileImage
    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    // Các getter/setter khác giữ nguyên
    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public int getAccountID() {
        return accountID;
    }

    public void setAccountID(int accountID) {
        this.accountID = accountID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public static class AccountRequest {
        private String username;
        private String password;
        private String email;
        private String profileImage;
        private String role;

        // Getters and Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginResponse {
        private int accountID;
        private String role;

        public LoginResponse(int accountID, String role) {
            this.accountID = accountID;
            this.role = role;
        }

        public int getAccountID() {
            return accountID;
        }

        public void setAccountID(int accountID) {
            this.accountID = accountID;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
    public static class AccountResponse {
        private int accountID;
        private String username;
        private String role;

        public AccountResponse(int accountID, String username, String role) {
            this.accountID = accountID;
            this.username = username;
            this.role = role;
        }

        // Getters and Setters
        public int getAccountID() { return accountID; }
        public void setAccountID(int accountID) { this.accountID = accountID; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    // DTO cho Change Password
    public static class ChangePasswordRequest {
        private int accountID;
        private String oldPassword;
        private String newPassword;

        public int getAccountID() { return accountID; }
        public void setAccountID(int accountID) { this.accountID = accountID; }
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String fullName;
        private String phone;
        private String email;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    public static class ForgotPasswordRequest {
        private String username;
        private String email;
        private String phone;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
    }

    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
