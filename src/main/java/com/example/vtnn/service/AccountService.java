package com.example.vtnn.service;

import com.example.vtnn.model.Account;
import com.example.vtnn.model.Customer;
import com.example.vtnn.model.Employee;
import com.example.vtnn.model.Product;
import com.example.vtnn.repository.AccountRepository;
import com.example.vtnn.repository.CustomerRepository;
import com.example.vtnn.repository.EmployeeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private JavaMailSender mailSender;

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public Account getAccountById(int id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
    }

    public Account updateAccount(int id, Account updatedAccount) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (!existingAccount.getUsername().equals(updatedAccount.getUsername()) &&
                accountRepository.findByUsername(updatedAccount.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }

        if (!existingAccount.getEmail().equals(updatedAccount.getEmail()) &&
                accountRepository.findByEmail(updatedAccount.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }

        if (!List.of("CUSTOMER", "ADMIN", "EMPLOYEE").contains(updatedAccount.getRole())) {
            throw new RuntimeException("Role không hợp lệ");
        }

        String currentRole = existingAccount.getRole();
        String newRole = updatedAccount.getRole();

        if ("CUSTOMER".equals(currentRole) && ("EMPLOYEE".equals(newRole) || "ADMIN".equals(newRole))) {
            throw new RuntimeException("Không thể đổi từ CUSTOMER sang EMPLOYEE hoặc ADMIN");
        }
        if (("EMPLOYEE".equals(currentRole) || "ADMIN".equals(currentRole)) && "CUSTOMER".equals(newRole)) {
            throw new RuntimeException("Không thể đổi từ EMPLOYEE hoặc ADMIN sang CUSTOMER");
        }

        // Xử lý ảnh cũ nếu thay đổi profileImage
        if (updatedAccount.getProfileImage() != null &&
                !updatedAccount.getProfileImage().equals(existingAccount.getProfileImage()) &&
                !updatedAccount.getProfileImage().equals("profile.jpg")) {
            String oldImage = existingAccount.getProfileImage();
            if (oldImage != null && !oldImage.equals("profile.jpg")) {
                boolean deleted = imageService.deleteImage(oldImage);
                if (!deleted) {
                    System.out.println("Không thể xóa ảnh cũ: " + oldImage);
                }
            }
        }

        existingAccount.setUsername(updatedAccount.getUsername());
        if (updatedAccount.getPassword() != null && !updatedAccount.getPassword().isEmpty()) {
            existingAccount.setPassword(updatedAccount.getPassword());
        }
        existingAccount.setEmail(updatedAccount.getEmail());
        existingAccount.setProfileImage(updatedAccount.getProfileImage() != null ? updatedAccount.getProfileImage() : existingAccount.getProfileImage());
        existingAccount.setRole(updatedAccount.getRole());

        return accountRepository.save(existingAccount);
    }

    public Account addAccount(Account account) {
        if (accountRepository.findByUsername(account.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (accountRepository.findByEmail(account.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (!List.of("CUSTOMER", "ADMIN", "EMPLOYEE").contains(account.getRole())) {
            throw new RuntimeException("Role không hợp lệ");
        }

        // Lưu Account trước để có accountID
        Account savedAccount = accountRepository.save(account);
        if (savedAccount.getAccountID() <= 0) {
            throw new RuntimeException("Lỗi khi lưu tài khoản: không tạo được accountID");
        }

        // Tạo Customer hoặc Employee dựa trên role
        if ("CUSTOMER".equals(savedAccount.getRole())) {
            Customer customer = new Customer();
            customer.setEmail(savedAccount.getEmail());
            customer.setCustomerName(savedAccount.getUsername());
            customer.setContactName(savedAccount.getUsername());
            customer.setAccount(savedAccount); // Chỉ cần gán quan hệ

            try {
                customerRepository.save(customer);
            } catch (Exception e) {
                accountRepository.delete(savedAccount);
                throw new RuntimeException("Lỗi khi tạo Customer: " + e.getMessage());
            }
        } else if ("EMPLOYEE".equals(savedAccount.getRole()) || "ADMIN".equals(savedAccount.getRole())) {
            Employee employee = new Employee();
            employee.setAccount(savedAccount);
            employee.setEmail(savedAccount.getEmail());
            employee.setFirstName(savedAccount.getUsername());
            employee.setLastName("");
            employee.setHireDate(new Date());

            try {
                employeeRepository.save(employee);
            } catch (Exception e) {
                accountRepository.delete(savedAccount);
                throw new RuntimeException("Lỗi khi tạo Employee: " + e.getMessage());
            }
        }

        return savedAccount;
    }
    public void unlockAccount(int id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        account.setLocked(false);
        accountRepository.save(account);
    }
    public void lockAccount(int id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        // Kiểm tra nếu là ADMIN thì không cho khóa
        if ("ADMIN".equals(account.getRole())) {
            throw new RuntimeException("Không thể khóa tài khoản ADMIN");
        }

        account.setLocked(true);
        accountRepository.save(account);
    }

    // Cập nhật phương thức login để kiểm tra trạng thái khóa
    public Account login(String username, String password) {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (account.isLocked()) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        if (!account.getPassword().equals(password)) {
            throw new RuntimeException("Mật khẩu không đúng");
        }

        return account;
    }

    public Page<AccountDTO> getAllAccounts(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        return accountRepository.findAll(pageable).map(AccountDTO::new);
    }

    public Page<Account> searchAccountsByUsername(String username, int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        return accountRepository.findByUsernameContainingIgnoreCase(username, pageable);
    }

    public void changePassword(int accountID, String oldPassword, String newPassword) {
        Account account = accountRepository.findById(accountID)
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        // Kiểm tra mật khẩu cũ
        if (!account.getPassword().equals(oldPassword)) {
            throw new RuntimeException("Mật khẩu cũ không đúng");
        }

        // Kiểm tra mật khẩu mới
        if (newPassword == null || newPassword.isEmpty()) {
            throw new RuntimeException("Mật khẩu mới không được để trống");
        }
        if (newPassword.length() < 6) {
            throw new RuntimeException("Mật khẩu mới không được ít hơn 6 ký tự");
        }

        account.setPassword(newPassword);
        accountRepository.save(account);
    }
    public Account register(String username, String password, String fullName, String phone, String email) {
        if (accountRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (username == null || username.isEmpty() || username.length() < 6) {
            throw new RuntimeException("Vui lòng nhập Username lớn hơn 6 ký tự");
        }
        if (accountRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (password == null || password.isEmpty() || password.length() < 6) {
            throw new RuntimeException("Vui lòng nhập password lớn hơn 6 ký tự");
        }

        // Tạo Account với role CUSTOMER
        Account account = new Account();
        account.setUsername(username);
        account.setPassword(password);
        account.setEmail(email);
        account.setRole("CUSTOMER");
        account.setCreatedDate(new Date());
        account.setProfileImage("profile.jpg");

        // Lưu Account và đảm bảo có accountID
        Account savedAccount = accountRepository.save(account);
        if (savedAccount.getAccountID() <= 0) {
            throw new RuntimeException("Lỗi khi lưu tài khoản: không tạo được accountID");
        }

        // Tạo Customer tương ứng
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setCustomerName(fullName);
        customer.setContactName(fullName);
        customer.setPhone(phone);
        customer.setAccount(savedAccount); // Chỉ cần gán quan hệ Account

        // Lưu Customer
        try {
            customerRepository.save(customer);
        } catch (Exception e) {
            // Nếu lưu Customer thất bại, xóa Account để tránh dữ liệu không nhất quán
            accountRepository.delete(savedAccount);
            throw new RuntimeException("Lỗi khi tạo Customer: " + e.getMessage());
        }

        return savedAccount;
    }
    // Inner class AccountDTO
    public static class AccountDTO {
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
    }

    public void forgotPassword(String username, String email, String phone) throws MessagingException {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (!account.getUsername().equals(username)) {
            throw new RuntimeException("Tên đăng nhập không khớp với email.");
        }

        boolean phoneMatches = false;
        List<Customer> customers = customerRepository.findByAccountAccountID(account.getAccountID());
        if (!customers.isEmpty() && customers.get(0).getPhone() != null && customers.get(0).getPhone().equals(phone)) {
            phoneMatches = true;
        }

        if (!phoneMatches) {
            Optional<Employee> employeeOpt = employeeRepository.findByAccountAccountID(account.getAccountID());
            if (employeeOpt.isPresent() && employeeOpt.get().getPhone() != null && employeeOpt.get().getPhone().equals(phone)) {
                phoneMatches = true;
            }
        }

        if (!phoneMatches) {
            throw new RuntimeException("Số điện thoại không khớp với tài khoản.");
        }

        String token = UUID.randomUUID().toString();
        account.setTokenCode(token);
        accountRepository.save(account);

        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        sendResetEmail(account.getEmail(), resetLink);
    }

    private void sendResetEmail(String to, String resetLink) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Bắt ngoại lệ UnsupportedEncodingException
            InternetAddress fromAddress;
            try {
                fromAddress = new InternetAddress("dhao3017@gmail.com", "FarmTech - Cửa Hàng Vật Tư Nông Nghiệp");
            } catch (UnsupportedEncodingException e) {
                logger.error("Lỗi encoding khi tạo InternetAddress: {}", e.getMessage(), e);
                // Fallback: Chỉ dùng email nếu encoding thất bại
                fromAddress = new InternetAddress("dhao3017@gmail.com");
            }
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Yêu Cầu Đặt Lại Mật Khẩu");

            String emailContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                      body { font-family: Arial, sans-serif; color: #333; text-align: center; }
                      .container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px; background-color: #f9f9f9; text-align: center; }
                      .header { background-color: #28a745; color: white; padding: 10px; text-align: center; border-radius: 5px 5px 0 0; }
                      .content { padding: 20px; text-align: center; }
                      .button { display: inline-block; padding: 10px 20px; background-color: #28a745; color: white !important; text-decoration: none; border-radius: 5px; font-weight: bold; }
                      .footer { text-align: center; font-size: 12px; color: #777; margin-top: 20px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>FarmTech</h1>
                        </div>
                        <div class="content">
                            <h2>Đặt Lại Mật Khẩu</h2>
                            <p>Xin chào,</p>
                            <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Vui lòng nhấp vào nút dưới đây để đặt lại mật khẩu:</p>
                            <p style="text-align: center;">
                               <a href="%s" class="button">Đặt Lại Mật Khẩu</a>
                            </p>
                            <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>
                            <p>Trân trọng,</p>
                            <p><strong>Đội ngũ FarmTech</strong></p>
                        </div>
                        <div class="footer">
                            <p>© 2025 FarmTech - Cửa Hàng Vật Tư Nông Nghiệp. Mọi quyền được bảo lưu.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(resetLink);

            helper.setText(emailContent, true);
            mailSender.send(message);
            logger.info("Email reset mật khẩu đã được gửi tới: {}", to);
        } catch (MessagingException e) {
            logger.error("Lỗi khi gửi email tới {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    public void resetPassword(String token, String newPassword) {
        Account account = accountRepository.findByTokenCode(token)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ hoặc đã hết hạn"));

        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Mật khẩu mới phải dài hơn 6 ký tự");
        }

        account.setPassword(newPassword);
        account.setTokenCode(null);
        accountRepository.save(account);
    }

    public List<Account> getAllAccountsNoPage() {
        return accountRepository.findAll();
    }
}