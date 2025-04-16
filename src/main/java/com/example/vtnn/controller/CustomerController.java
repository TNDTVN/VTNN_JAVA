package com.example.vtnn.controller;

import com.example.vtnn.DTO.CustomerDTO;
import com.example.vtnn.model.Customer;
import com.example.vtnn.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<Page<CustomerDTO>> getAllCustomers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "customerID,asc") String sort
    ) {
        Page<CustomerDTO> customerDTOS = customerService.getAllCustomers(page, size, sort);
        return ResponseEntity.ok(customerDTOS);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDTO>> searchCustomers(
            @RequestParam("name") String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "customerID,asc") String sort
    ) {
        Page<CustomerDTO> customers = customerService.searchCustomerByName(name, page, size, sort);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/all")
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomersNoPage();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable("id") int id) {
        Customer customer = customerService.getCustomerById(id);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new CustomerDTO(customer));
    }

    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody CustomerDTO.CustomerCreateDTO customerDTO) {
        try {
            Customer savedCustomer = customerService.createCustomerFromDTO(customerDTO);
            return ResponseEntity.ok(savedCustomer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable("id") int id,
            @RequestBody Map<String, Object> updates
    ) {
        Customer updatedCustomer = customerService.updateCustomer(id, updates);
        return ResponseEntity.ok(updatedCustomer);
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<String> lockCustomer(@PathVariable("id") int id) {
        try {
            customerService.lockCustomerAccount(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/unlock")
    public ResponseEntity<String> unlockCustomer(@PathVariable("id") int id) {
        try {
            customerService.unlockCustomerAccount(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable("id") int id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Lấy thông tin khách hàng theo accountID
    @GetMapping("/account/{accountID}")
    public ResponseEntity<Customer> getCustomerByAccountID(@PathVariable int accountID) {
        try {
            Customer customer = customerService.getCustomerByAccountID(accountID);
            return ResponseEntity.ok(customer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/profile/{customerID}")
    public ResponseEntity<?> updateCustomerProfile(
            @PathVariable int customerID,
            @RequestBody CustomerDTO.CustomerUpdateRequest customerUpdateRequest
    ) {
        try {
            Customer updatedCustomer = customerService.updateCustomer(
                    customerID,
                    customerUpdateRequest.getCustomerName(),
                    customerUpdateRequest.getContactName(),
                    customerUpdateRequest.getAddress(),
                    customerUpdateRequest.getCity(),
                    customerUpdateRequest.getPostalCode(),
                    customerUpdateRequest.getCountry(),
                    customerUpdateRequest.getPhone(),
                    customerUpdateRequest.getEmail()
            );
            return ResponseEntity.ok(updatedCustomer);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}