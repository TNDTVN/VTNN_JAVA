package com.example.vtnn.service;

import com.example.vtnn.DTO.CustomerDTO;
import com.example.vtnn.model.Account;
import com.example.vtnn.model.Customer;
import com.example.vtnn.repository.AccountRepository;
import com.example.vtnn.repository.CustomerRepository;
import com.example.vtnn.repository.OrderRepository; // Thêm để kiểm tra liên kết với hóa đơn
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository,
                           AccountRepository accountRepository,
                           OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.orderRepository = orderRepository;
    }

    public Customer getCustomerById(int id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));
    }

    public Page<CustomerDTO> getAllCustomers(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        return customerRepository.findAll(pageable).map(CustomerDTO::new);
    }

    public Page<CustomerDTO> searchCustomerByName(String name, int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        return customerRepository.findByCustomerNameOrContactNameContainingIgnoreCase(name, pageable)
                .map(CustomerDTO::new);
    }

    public List<Customer> getAllCustomersNoPage() {
        return customerRepository.findAll();
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(int id, Map<String, Object> updates) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        if (updates.containsKey("customerName")) {
            existingCustomer.setCustomerName((String) updates.get("customerName"));
        }
        if (updates.containsKey("contactName")) {
            existingCustomer.setContactName((String) updates.get("contactName"));
        }
        if (updates.containsKey("address")) {
            existingCustomer.setAddress((String) updates.get("address"));
        }
        if (updates.containsKey("city")) {
            existingCustomer.setCity((String) updates.get("city"));
        }
        if (updates.containsKey("postalCode")) {
            existingCustomer.setPostalCode((String) updates.get("postalCode"));
        }
        if (updates.containsKey("country")) {
            existingCustomer.setCountry((String) updates.get("country"));
        }
        if (updates.containsKey("phone")) {
            existingCustomer.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("email")) {
            existingCustomer.setEmail((String) updates.get("email"));
        }

        return customerRepository.save(existingCustomer);
    }

    @Transactional
    public Customer createCustomerFromDTO(CustomerDTO.CustomerCreateDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            throw new RuntimeException("Tên đăng nhập là bắt buộc khi tạo khách hàng mới");
        }

        if (accountRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email '" + dto.getEmail() + "' đã tồn tại");
        }
        if (accountRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Tên đăng nhập '" + dto.getUsername() + "' đã tồn tại");
        }

        Account account = new Account(
                dto.getUsername(),
                "Password123",
                dto.getEmail(),
                "profile.jpg",
                new Date(),
                "CUSTOMER"
        );
        Account savedAccount = accountRepository.save(account);

        if (savedAccount.getAccountID() == 0) {
            throw new RuntimeException("Không thể lưu Account đúng cách");
        }

        Customer customer = new Customer();
        customer.setCustomerName(dto.getCustomerName());
        customer.setContactName(dto.getContactName());
        customer.setAddress(dto.getAddress());
        customer.setCity(dto.getCity());
        customer.setPostalCode(dto.getPostalCode());
        customer.setCountry(dto.getCountry());
        customer.setPhone(dto.getPhone());
        customer.setEmail(dto.getEmail());
        customer.setAccount(savedAccount);

        return customerRepository.save(customer);
    }

    public void lockCustomerAccount(int id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        Account account = customer.getAccount();
        if (account == null) {
            throw new RuntimeException("Tài khoản liên kết với khách hàng không tồn tại");
        }

        account.setLocked(true);
        accountRepository.save(account);
    }

    public void unlockCustomerAccount(int id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        Account account = customer.getAccount();
        if (account == null) {
            throw new RuntimeException("Tài khoản liên kết với khách hàng không tồn tại");
        }

        account.setLocked(false);
        accountRepository.save(account);
    }

    public void deleteCustomer(int id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        Account account = customer.getAccount();
        if (account == null) {
            throw new RuntimeException("Tài khoản liên kết với khách hàng không tồn tại");
        }

        if (orderRepository.existsByCustomerCustomerID(id)) {
            throw new RuntimeException("Không thể xóa khách hàng đã có hóa đơn");
        }

        accountRepository.delete(account);
        customerRepository.delete(customer);
    }
    public Customer getCustomerByAccountID(int accountID) {
        return customerRepository.findByAccountID(accountID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với accountID: " + accountID));
    }

    public Customer updateCustomer(int customerID, String customerName, String contactName, String address,
                                   String city, String postalCode, String country, String phone, String email) {
        Customer customer = customerRepository.findById(customerID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + customerID));

        customer.setCustomerName(customerName);
        customer.setContactName(contactName);
        customer.setAddress(address);
        customer.setCity(city);
        customer.setPostalCode(postalCode);
        customer.setCountry(country);
        customer.setPhone(phone);
        customer.setEmail(email);

        return customerRepository.save(customer);
    }
}