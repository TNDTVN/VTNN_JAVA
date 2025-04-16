package com.example.vtnn.service;

import com.example.vtnn.DTO.EmployeeDTO;
import com.example.vtnn.model.Account;
import com.example.vtnn.model.Employee;
import com.example.vtnn.repository.AccountRepository;
import com.example.vtnn.repository.OrderRepository;
import com.example.vtnn.repository.EmployeeRepository;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final OrderRepository orderRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           AccountRepository accountRepository,
                           OrderRepository orderRepository) {
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
        this.orderRepository = orderRepository;
    }

    public Employee getEmployeeById(int id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
    }

    public Page<EmployeeDTO> getAllAccounts(int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        return employeeRepository.findAll(pageable).map(EmployeeDTO::new);
    }

    public Page<EmployeeDTO> searchEmployeeByName(String name, int page, int size, String sort) {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortField));
        return employeeRepository.findByFirstNameOrLastNameContainingIgnoreCase(name, pageable)
                .map(EmployeeDTO::new);
    }

    public List<Employee> getAllEmployeesNoPage() {
        return employeeRepository.findAll();
    }

    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }
    public Employee updateEmployee(int id, Map<String, Object> updates) {
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        if (updates.containsKey("firstName")) {
            existingEmployee.setFirstName((String) updates.get("firstName"));
        }
        if (updates.containsKey("lastName")) {
            existingEmployee.setLastName((String) updates.get("lastName"));
        }
        if (updates.containsKey("birthDate")) {
            existingEmployee.setBirthDate(parseDate((String) updates.get("birthDate")));
        }
        if (updates.containsKey("hireDate")) {
            existingEmployee.setHireDate(parseDate((String) updates.get("hireDate")));
        }
        if (updates.containsKey("address")) {
            existingEmployee.setAddress((String) updates.get("address"));
        }
        if (updates.containsKey("city")) {
            existingEmployee.setCity((String) updates.get("city"));
        }
        if (updates.containsKey("postalCode")) {
            existingEmployee.setPostalCode((String) updates.get("postalCode"));
        }
        if (updates.containsKey("country")) {
            existingEmployee.setCountry((String) updates.get("country"));
        }
        if (updates.containsKey("phone")) {
            existingEmployee.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("email")) {
            existingEmployee.setEmail((String) updates.get("email"));
        }

        return employeeRepository.save(existingEmployee);
    }


    public Employee createEmployeeFromDTO(EmployeeDTO.EmployeeCreateDTO dto) {
        System.out.println("DTO nhận được: " + dto);
        if (dto.getUsername() == null || dto.getUsername().isEmpty()) {
            throw new RuntimeException("Tên đăng nhập là bắt buộc khi tạo nhân viên mới");
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
                "EMPLOYEE"
        );
        Account savedAccount = accountRepository.save(account);

        Employee employee = new Employee();
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setBirthDate(dto.getBirthDate());
        employee.setHireDate(dto.getHireDate());
        employee.setAddress(dto.getAddress());
        employee.setCity(dto.getCity());
        employee.setPostalCode(dto.getPostalCode());
        employee.setCountry(dto.getCountry());
        employee.setPhone(dto.getPhone());
        employee.setEmail(dto.getEmail());
        employee.setUsername(dto.getUsername());
        employee.setAccount(savedAccount);

        return employeeRepository.save(employee);
    }

    public void lockEmployeeAccount(int id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
        Account account = employee.getAccount();
        if (account == null) {
            throw new RuntimeException("Tài khoản liên kết với nhân viên không tồn tại");
        }
        if ("ADMIN".equals(account.getRole())) {
            throw new RuntimeException("Không thể khóa tài khoản ADMIN");
        }
        account.setLocked(true);
        accountRepository.save(account);
    }

    public void unlockEmployeeAccount(int id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        Account account = employee.getAccount();
        if (account == null) {
            throw new RuntimeException("Tài khoản liên kết với nhân viên không tồn tại");
        }

        account.setLocked(false);
        accountRepository.save(account);
    }

    public void deleteEmployee(int id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        Account account = employee.getAccount();
        if (account == null) {
            throw new RuntimeException("Tài khoản liên kết với nhân viên không tồn tại");
        }

        if ("ADMIN".equals(account.getRole())) {
            throw new RuntimeException("Không thể xóa tài khoản ADMIN");
        }

        if (orderRepository.existsByEmployeeEmployeeID(id)) {
            throw new RuntimeException("Không thể xóa nhân viên đã lập hóa đơn");
        }

        accountRepository.delete(account);
        employeeRepository.delete(employee);
    }
    public Employee getEmployeeByAccountID(int accountID) {
        return employeeRepository.findByAccountAccountID(accountID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với accountID: " + accountID));
    }

    public Employee updateEmployee(int employeeID, String firstName, String lastName, String birthDate, String hireDate,
                                   String address, String city, String postalCode, String country, String phone, String email) {
        Employee employee = employeeRepository.findById(employeeID)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + employeeID));

        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        if (birthDate != null && !birthDate.isEmpty()) {
            employee.setBirthDate(parseDate(birthDate));
        }
        if (hireDate != null && !hireDate.isEmpty()) {
            employee.setHireDate(parseDate(hireDate));
        }
        employee.setAddress(address);
        employee.setCity(city);
        employee.setPostalCode(postalCode);
        employee.setCountry(country);
        employee.setPhone(phone);
        employee.setEmail(email);

        return employeeRepository.save(employee);
    }

    // Hàm hỗ trợ chuyển chuỗi ISO thành Date
    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException("Định dạng ngày không hợp lệ: " + dateStr);
        }
    }
}
