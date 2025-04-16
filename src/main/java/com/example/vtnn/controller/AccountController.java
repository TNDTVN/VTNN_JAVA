package com.example.vtnn.controller;

import com.example.vtnn.DTO.AccountDTO.AccountRequest;
import com.example.vtnn.DTO.AccountDTO;
import com.example.vtnn.DTO.AccountDTO.LoginResponse;
import com.example.vtnn.DTO.AccountDTO.LoginRequest;
import com.example.vtnn.DTO.AccountDTO.AccountResponse;
import com.example.vtnn.DTO.AccountDTO.RegisterRequest;
import com.example.vtnn.DTO.AccountDTO.ChangePasswordRequest;
import com.example.vtnn.DTO.AccountDTO.ForgotPasswordRequest;
import com.example.vtnn.DTO.AccountDTO.ResetPasswordRequest;
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
    public ResponseEntity<Page<AccountDTO>> getAllAccounts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size, // Đổi thành 5 để khớp với frontend
            @RequestParam(defaultValue = "accountID,asc") String sort // Mặc định asc
    ) {
        Page<AccountDTO> accounts = accountService.getAllAccounts(page, size, sort);
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

    @GetMapping("/customer")
    public ResponseEntity<List<Account>> getAccountsByRole(@RequestParam String role) {
        List<Account> accounts = accountRepository.findByRole(role);
        return ResponseEntity.ok(accounts);
    }
}