package com.example.vtnn.controller;

import com.example.vtnn.model.Account;
import com.example.vtnn.repository.AccountRepository;
import com.example.vtnn.service.AccountService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @PostMapping
    public ResponseEntity<?> addAccount(@RequestBody AccountRequest accountRequest) {
        try {
            Account account = new Account(
                    accountRequest.getUsername(),
                    accountRequest.getPassword(),
                    accountRequest.getEmail(),
                    accountRequest.getProfileImage(),
                    new Date(),
                    accountRequest.getRole()
            );
            Account savedAccount = accountService.addAccount(account);
            return ResponseEntity.ok(new AccountResponse(savedAccount.getAccountID(), savedAccount.getUsername(), savedAccount.getRole()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<Account> getAccountById(@PathVariable("id") int id) {
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(account);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAccount(@PathVariable("id") int id, @RequestBody AccountRequest accountRequest) {
        try {
            Account account = new Account(
                    accountRequest.getUsername(),
                    accountRequest.getPassword(),
                    accountRequest.getEmail(),
                    accountRequest.getProfileImage(),
                    new Date(),
                    accountRequest.getRole()
            );
            Account updatedAccount = accountService.updateAccount(id, account);
            return ResponseEntity.ok(new AccountResponse(updatedAccount.getAccountID(), updatedAccount.getUsername(), updatedAccount.getRole()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Account account = accountService.login(loginRequest.getUsername(), loginRequest.getPassword());
            return ResponseEntity.ok(new LoginResponse(account.getAccountID(), account.getRole()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/{id}/lock")
    public ResponseEntity<String> lockAccount(@PathVariable("id") int id) {
        try {
            accountService.lockAccount(id);
            return ResponseEntity.ok("Khóa tài khoản thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            Account savedAccount = accountService.register(
                    registerRequest.getUsername(),
                    registerRequest.getPassword(),
                    registerRequest.getFullName(),
                    registerRequest.getPhone(),
                    registerRequest.getEmail()
            );
            return ResponseEntity.ok(new AccountResponse(savedAccount.getAccountID(), savedAccount.getUsername(), savedAccount.getRole()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            accountService.changePassword(
                    changePasswordRequest.getAccountID(),
                    changePasswordRequest.getOldPassword(),
                    changePasswordRequest.getNewPassword()
            );
            return ResponseEntity.ok("Đổi mật khẩu thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/{id}/unlock")
    public ResponseEntity<String> unlockAccount(@PathVariable("id") int id) {
        try {
            accountService.unlockAccount(id);
            return ResponseEntity.ok("Mở khóa tài khoản thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<Page<AccountService.AccountDTO>> getAllAccounts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size, // Đổi thành 5 để khớp với frontend
            @RequestParam(defaultValue = "accountID,asc") String sort // Mặc định asc
    ) {
        Page<AccountService.AccountDTO> accounts = accountService.getAllAccounts(page, size, sort);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Account>> searchAccounts(
            @RequestParam("username") String username,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size, // Đổi thành 5 để khớp với frontend
            @RequestParam(defaultValue = "accountID,asc") String sort // Mặc định asc
    ) {
        Page<Account> accounts = accountService.searchAccountsByUsername(username, page, size, sort);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/all")
    public List<Account> getAllAccounts() {
        return accountService.getAllAccountsNoPage();
    }
    // DTO cho request
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

    // DTO cho response
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

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            accountService.forgotPassword(request.getUsername(), request.getEmail(), request.getPhone());
            return ResponseEntity.ok("Link đặt lại mật khẩu đã được gửi qua email.");
        } catch (RuntimeException e) {
            logger.warn("Lỗi Runtime khi xử lý forgot-password: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (MessagingException e) {
            logger.error("Lỗi gửi email trong forgot-password: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Lỗi khi gửi email: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            accountService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Đặt lại mật khẩu thành công.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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

    @GetMapping("/customer")
    public ResponseEntity<List<Account>> getAccountsByRole(@RequestParam String role) {
        List<Account> accounts = accountRepository.findByRole(role);
        return ResponseEntity.ok(accounts);
    }
}