package com.example.vtnn.controller;

import com.example.vtnn.model.Employee;
import com.example.vtnn.service.EmployeeService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<Page<EmployeeService.EmployeeDTO>> getAllEmployees(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "employeeID,asc") String sort
    ) {
        Page<EmployeeService.EmployeeDTO> employeeDTOS = employeeService.getAllAccounts(page, size, sort);
        return ResponseEntity.ok(employeeDTOS);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<EmployeeService.EmployeeDTO>> searchEmployees(
            @RequestParam("name") String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "employeeID,asc") String sort
    ) {
        Page<EmployeeService.EmployeeDTO> employees = employeeService.searchEmployeeByName(name, page, size, sort);
        return ResponseEntity.ok(employees);
    }

    @GetMapping("/all")
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployeesNoPage();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeService.EmployeeDTO> getEmployeeById(@PathVariable("id") int id) {
        Employee employee = employeeService.getEmployeeById(id);
        if (employee == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new EmployeeService.EmployeeDTO(employee));
    }

    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody EmployeeService.EmployeeCreateDTO employeeDTO) {
        try {
            Employee savedEmployee = employeeService.createEmployeeFromDTO(employeeDTO);
            return ResponseEntity.ok(savedEmployee);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}
    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable("id") int id,
            @RequestBody Map<String, Object> updates // Nhận dữ liệu dưới dạng Map thay vì Employee
    ) {
        Employee updatedEmployee = employeeService.updateEmployee(id, updates);
        return ResponseEntity.ok(updatedEmployee);
    }
    @PostMapping("/{id}/lock")
    public ResponseEntity<String> lockEmployee(@PathVariable("id") int id) {
        try {
            employeeService.lockEmployeeAccount(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/unlock")
    public ResponseEntity<String> unlockEmployee(@PathVariable("id") int id) {
        try {
            employeeService.unlockEmployeeAccount(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable("id") int id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/account/{accountID}")
    public ResponseEntity<Employee> getEmployeeByAccountID(@PathVariable int accountID) {
        try {
            Employee employee = employeeService.getEmployeeByAccountID(accountID);
            return ResponseEntity.ok(employee);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PutMapping("/profile/{employeeID}")
    public ResponseEntity<?> updateEmployeeProfile(
            @PathVariable int employeeID,
            @RequestBody EmployeeUpdateRequest employeeUpdateRequest
    ) {
        try {
            Employee updatedEmployee = employeeService.updateEmployee(
                    employeeID,
                    employeeUpdateRequest.getFirstName(),
                    employeeUpdateRequest.getLastName(),
                    employeeUpdateRequest.getBirthDate(),
                    employeeUpdateRequest.getHireDate(),
                    employeeUpdateRequest.getAddress(),
                    employeeUpdateRequest.getCity(),
                    employeeUpdateRequest.getPostalCode(),
                    employeeUpdateRequest.getCountry(),
                    employeeUpdateRequest.getPhone(),
                    employeeUpdateRequest.getEmail()
            );
            return ResponseEntity.ok(updatedEmployee);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    public static class EmployeeUpdateRequest {
        private String firstName;
        private String lastName;
        private String birthDate; // ISO Date String (YYYY-MM-DD)
        private String hireDate;  // ISO Date String (YYYY-MM-DD)
        private String address;
        private String city;
        private String postalCode;
        private String country;
        private String phone;
        private String email;

        // Getters and Setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getBirthDate() { return birthDate; }
        public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
        public String getHireDate() { return hireDate; }
        public void setHireDate(String hireDate) { this.hireDate = hireDate; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}