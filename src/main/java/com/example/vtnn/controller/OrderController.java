package com.example.vtnn.controller;

import com.example.vtnn.model.Employee;
import com.example.vtnn.model.Order;
import com.example.vtnn.model.Product;
import com.example.vtnn.repository.EmployeeRepository;
import com.example.vtnn.repository.OrderRepository;
import com.example.vtnn.repository.ProductRepository;
import com.example.vtnn.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<Page<Order>> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "orderID,asc") String sort,
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestHeader(value = "X-Account-ID", required = false) Integer accountID
    ) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);

        // Tạo Pageable không có Sort, để OrderService xử lý
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Order> orders = orderService.getOrders(pageable, role, accountID, sortField, direction);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable int id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Order>> searchOrders(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "orderID,asc") String sort,
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestHeader(value = "X-Account-ID", required = false) Integer accountID
    ) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortParams[0]));
        Page<Order> orders = orderService.searchOrders(keyword, pageable, role, accountID);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<Order>> getPendingOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "orderID,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortParams[0]));
        Page<Order> orders = orderService.getPendingOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/pending/search")
    public ResponseEntity<Page<Order>> searchPendingOrders(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "orderID,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortParams[0]));
        Page<Order> orders = orderService.searchPendingOrders(keyword, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/cancelled")
    public ResponseEntity<Page<Order>> getCancelledOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "orderID,asc") String sort,
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestHeader(value = "X-Account-ID", required = false) Integer accountID
    ) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortParams[0]));
        Page<Order> orders = orderService.getCancelledOrders(pageable, role, accountID);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/cancelled/search")
    public ResponseEntity<Page<Order>> searchCancelledOrders(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "orderID,asc") String sort,
            @RequestHeader(value = "X-Role", required = false) String role,
            @RequestHeader(value = "X-Account-ID", required = false) Integer accountID
    ) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortParams[0]));
        Page<Order> orders = orderService.searchCancelledOrders(keyword, pageable, role, accountID);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Order> approveOrder(
            @PathVariable int id,
            @RequestBody Map<String, Integer> body) {
        int accountId = body.get("accountId");
        Order order = orderService.approveOrder(id, accountId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(
            @PathVariable int id,
            @RequestBody Map<String, Object> body) {
        int accountId = (int) body.get("accountId");
        String notes = (String) body.get("notes");
        Order order = orderService.cancelOrder(id, accountId, notes);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatsResponse> getStatistics(@RequestParam(required = false) Integer accountID) {
        List<Order> orders;
        if (accountID != null) {
            // Ánh xạ accountID sang employeeID
            Employee employee = employeeRepository.findByAccountID(accountID)
                    .orElseThrow(() -> new RuntimeException("Employee not found for accountID: " + accountID));
            orders = orderRepository.findByEmployeeIDAndIsCancelledFalseAndEmployeeIDNotNull(employee.getEmployeeID());
        } else {
            orders = orderRepository.findByIsCancelledFalseAndEmployeeIDNotNull();
        }

        long totalOrders = orders.size();
        BigDecimal totalRevenue = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<OrderStatsDTO> orderStats = orders.stream()
                .map(order -> {
                    Employee employee = employeeRepository.findById(order.getEmployeeID()).orElse(null);
                    String employeeName = employee != null ? employee.getFirstName() + " " + employee.getLastName() : "Unknown";
                    return new OrderStatsDTO(
                            order.getOrderID(),
                            order.getOrderDate(),
                            order.getTotalPrice(),
                            order.getEmployeeID(),
                            employeeName
                    );
                })
                .collect(Collectors.toList());

        StatsResponse response = new StatsResponse(totalOrders, totalRevenue, orderStats);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{accountID}")
    public ResponseEntity<List<Product>> getPurchasedProducts(@PathVariable Integer accountID) {
        List<Product> purchasedProducts = orderService.getPurchasedProducts(accountID);
        return ResponseEntity.ok(purchasedProducts);
    }

    // Endpoint để lọc sản phẩm đã mua, sử dụng OrderService
    @GetMapping("/products/{accountID}/filter")
    public ResponseEntity<Page<Product>> filterPurchasedProducts(
            @PathVariable Integer accountID,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> productPage = orderService.filterPurchasedProducts(
                accountID, categoryId, keyword, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(productPage);
    }
    @GetMapping("/history")
    public ResponseEntity<Page<Order>> getPurchaseHistory(
            @RequestHeader(value = "X-Account-ID", required = true) Integer accountID,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "orderDate,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = Sort.Direction.fromString(sortParams[1]);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));
        Page<Order> orders = orderService.getPurchaseHistory(accountID, pageable);
        return ResponseEntity.ok(orders);
    }
    // Cập nhật DTO
    record StatsResponse(long totalOrders, BigDecimal totalRevenue, List<OrderStatsDTO> orders) {}
    record OrderStatsDTO(int orderID, Date orderDate, BigDecimal totalPrice, Integer employeeID, String employeeName) {}
}