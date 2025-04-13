package com.example.vtnn.service;

import com.example.vtnn.model.*;
import com.example.vtnn.repository.CustomerRepository;
import com.example.vtnn.repository.EmployeeRepository;
import com.example.vtnn.repository.OrderRepository;
import com.example.vtnn.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public Page<Order> getOrders(Pageable pageable, String role, Integer accountID, String sortField, Sort.Direction sortDirection) {
        logger.info("Sort field: {}, Direction: {}", sortField, sortDirection);

        // Tạo Pageable không có Sort ban đầu
        Pageable pageableWithoutSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        if ("EMPLOYEE".equals(role) && accountID != null) {
            Employee employee = employeeRepository.findByAccountID(accountID)
                    .orElseThrow(() -> new RuntimeException("Employee not found for accountID: " + accountID));
            if ("totalAmount".equals(sortField)) {
                // Sắp xếp theo totalAmount cho nhân viên cụ thể
                if (sortDirection.isDescending()) {
                    return orderRepository.findByEmployeeEmployeeIDAndEmployeeIDNotNullAndIsCancelledFalseSortedByTotalAmountDesc(employee.getEmployeeID(), pageableWithoutSort);
                } else {
                    return orderRepository.findByEmployeeEmployeeIDAndEmployeeIDNotNullAndIsCancelledFalseSortedByTotalAmountAsc(employee.getEmployeeID(), pageableWithoutSort);
                }
            } else {
                // Sắp xếp theo các trường hợp lệ khác
                Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortDirection, sortField);
                return orderRepository.findByEmployeeEmployeeIDAndEmployeeIDNotNullAndIsCancelledFalse(employee.getEmployeeID(), sortedPageable);
            }
        }

        // Trường hợp ADMIN hoặc mặc định
        if ("totalAmount".equals(sortField)) {
            if (sortDirection.isDescending()) {
                return orderRepository.findAllSortedByTotalAmountDesc(pageableWithoutSort);
            } else {
                return orderRepository.findAllSortedByTotalAmountAsc(pageableWithoutSort);
            }
        }

        // Các trường hợp khác sử dụng Sort hợp lệ
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortDirection, sortField);
        return orderRepository.findByEmployeeIDNotNullAndIsCancelledFalse(sortedPageable);
    }

    // Lấy hóa đơn theo ID
    public Order getOrderById(int id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    // Tìm kiếm hóa đơn đã duyệt (giữ nguyên logic hiện tại cho trang Order)
    public Page<Order> searchOrders(String keyword, Pageable pageable, String role, Integer accountID) {
        String sortField = pageable.getSort().isEmpty() ? "orderID" : pageable.getSort().iterator().next().getProperty();

        try {
            int id = Integer.parseInt(keyword);
            if ("EMPLOYEE".equals(role) && accountID != null) {
                Employee employee = employeeRepository.findByAccountID(accountID)
                        .orElseThrow(() -> new RuntimeException("Employee not found for accountID: " + accountID));
                return orderRepository.findByEmployeeEmployeeIDAndEmployeeIDNotNullAndIsCancelledFalse(employee.getEmployeeID(), pageable);
            }
            if (orderRepository.existsByEmployeeEmployeeID(id)) {
                return orderRepository.findByEmployeeEmployeeIDAndEmployeeIDNotNullAndIsCancelledFalse(id, pageable);
            } else if (orderRepository.existsByCustomerCustomerID(id)) {
                return orderRepository.findByCustomerCustomerIDAndEmployeeIDNotNullAndIsCancelledFalse(id, pageable);
            }
        } catch (NumberFormatException e) {
            if ("EMPLOYEE".equals(role) && accountID != null) {
                Employee employee = employeeRepository.findByAccountID(accountID)
                        .orElseThrow(() -> new RuntimeException("Employee not found for accountID: " + accountID));
                return orderRepository.findByEmployeeEmployeeIDAndEmployeeIDNotNullAndIsCancelledFalse(employee.getEmployeeID(), pageable);
            }
            return orderRepository.findByCustomerCustomerNameContainingOrEmployeeFirstNameContainingAndEmployeeIDNotNullAndIsCancelledFalse(
                    keyword, keyword, pageable);
        }
        return orderRepository.findByEmployeeIDNotNullAndIsCancelledFalse(pageable);
    }

    // Lấy danh sách hóa đơn chưa duyệt
    public Page<Order> getPendingOrders(Pageable pageable) {
        return orderRepository.findByEmployeeIDIsNullAndIsCancelledFalse(pageable);
    }

    // Tìm kiếm hóa đơn chưa duyệt
    public Page<Order> searchPendingOrders(String keyword, Pageable pageable) {
        try {
            int id = Integer.parseInt(keyword);
            if (orderRepository.existsByCustomerCustomerID(id)) {
                return orderRepository.findByCustomerCustomerIDAndEmployeeIDIsNullAndIsCancelledFalse(id, pageable);
            }
        } catch (NumberFormatException e) {
            return orderRepository.findByCustomerCustomerNameContainingAndEmployeeIDIsNullAndIsCancelledFalse(keyword, pageable);
        }
        return orderRepository.findByEmployeeIDIsNullAndIsCancelledFalse(pageable);
    }

    // Lấy danh sách hóa đơn đã hủy
    public Page<Order> getCancelledOrders(Pageable pageable, String role, Integer accountID) {
        if ("EMPLOYEE".equals(role) && accountID != null) {
            Employee employee = employeeRepository.findByAccountID(accountID)
                    .orElseThrow(() -> new RuntimeException("Employee not found for accountID: " + accountID));
            return orderRepository.findByEmployeeEmployeeIDAndIsCancelledTrue(employee.getEmployeeID(), pageable);
        }
        // Nếu là ADMIN hoặc không có role/accountID, trả về tất cả hóa đơn đã hủy
        return orderRepository.findByIsCancelledTrue(pageable);
    }

    // Tìm kiếm hóa đơn đã hủy
    public Page<Order> searchCancelledOrders(String keyword, Pageable pageable, String role, Integer accountID) {
        if ("EMPLOYEE".equals(role) && accountID != null) {
            Employee employee = employeeRepository.findByAccountID(accountID)
                    .orElseThrow(() -> new RuntimeException("Employee not found for accountID: " + accountID));
            try {
                int id = Integer.parseInt(keyword);
                if (orderRepository.existsByCustomerCustomerID(id)) {
                    return orderRepository.findByCustomerCustomerIDAndEmployeeEmployeeIDAndIsCancelledTrue(id, employee.getEmployeeID(), pageable);
                }
            } catch (NumberFormatException e) {
                return orderRepository.findByCustomerCustomerNameContainingAndEmployeeEmployeeIDAndIsCancelledTrue(keyword, employee.getEmployeeID(), pageable);
            }
            return orderRepository.findByEmployeeEmployeeIDAndIsCancelledTrue(employee.getEmployeeID(), pageable);
        }
        // Nếu là ADMIN, tìm kiếm tất cả hóa đơn đã hủy
        try {
            int id = Integer.parseInt(keyword);
            if (orderRepository.existsByCustomerCustomerID(id)) {
                return orderRepository.findByCustomerCustomerIDAndIsCancelledTrue(id, pageable);
            } else if (orderRepository.existsByEmployeeEmployeeID(id)) {
                return orderRepository.findByEmployeeEmployeeIDAndIsCancelledTrue(id, pageable);
            }
        } catch (NumberFormatException e) {
            return orderRepository.findByCustomerCustomerNameContainingOrEmployeeFirstNameContainingAndIsCancelledTrue(keyword, keyword, pageable);
        }
        return orderRepository.findByIsCancelledTrue(pageable);
    }

    // Duyệt hóa đơn
    public Order approveOrder(int orderId, int accountId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getEmployeeID() != null) {
            throw new RuntimeException("Order already approved");
        }

        Employee employee = employeeRepository.findByAccountID(accountId)
                .orElseThrow(() -> new RuntimeException("Employee not found for accountID: " + accountId));

        order.setEmployeeID(employee.getEmployeeID());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 3);
        order.setShippedDate(calendar.getTime());
        return orderRepository.save(order);
    }

    // Hủy hóa đơn
    @Transactional
    public Order cancelOrder(int orderId, int accountId, String notes) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.isCancelled()) {
            throw new RuntimeException("Order already cancelled");
        }

        Employee employee = employeeRepository.findByAccountID(accountId)
                .orElseThrow(() -> new RuntimeException("Employee not found for accountID: " + accountId));

        for (OrderDetail detail : order.getOrderDetails()) {
            Product product = productRepository.findById(detail.getProductID())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + detail.getProductID()));

            int newStock = product.getUnitsInStock() + detail.getQuantity();
            product.setUnitsInStock(newStock);
            productRepository.save(product);
        }

        order.setCancelled(true);
        order.setEmployeeID(employee.getEmployeeID());
        order.setNotes(notes);
        return orderRepository.save(order);
    }

    public List<Product> getPurchasedProducts(Integer accountID) {
        // Tìm Customer theo accountID
        Customer customer = customerRepository.findByAccountID(accountID)
                .orElseThrow(() -> new RuntimeException("Customer not found for accountID: " + accountID));

        // Lấy danh sách đơn hàng theo customerID
        List<Order> orders = orderRepository.findByCustomerCustomerIDAndIsCancelledFalse(customer.getCustomerID());
        List<Product> purchasedProducts = new ArrayList<>();

        for (Order order : orders) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Product product = detail.getProduct();
                if (!purchasedProducts.stream().anyMatch(p -> p.getProductID() == product.getProductID())) {
                    purchasedProducts.add(product);
                }
            }
        }
        return purchasedProducts;
    }

    public Page<Product> filterPurchasedProducts(
            Integer accountID,
            Integer categoryId,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        List<Product> purchasedProducts = getPurchasedProducts(accountID);

        List<Product> filteredProducts = purchasedProducts.stream()
                .filter(product -> categoryId == null || product.getCategoryID() == categoryId)
                .filter(product -> keyword == null || product.getProductName().toLowerCase().contains(keyword.toLowerCase()))
                .filter(product -> minPrice == null || product.getUnitPrice().compareTo(minPrice) >= 0)
                .filter(product -> maxPrice == null || product.getUnitPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredProducts.size());
        List<Product> pagedProducts = filteredProducts.subList(start, end);

        return new PageImpl<>(pagedProducts, pageable, filteredProducts.size());
    }
    public Page<Order> getPurchaseHistory(Integer accountID, Pageable pageable) {
        Customer customer = customerRepository.findByAccountID(accountID)
                .orElseThrow(() -> new RuntimeException("Customer not found for accountID: " + accountID));
        // Loại bỏ điều kiện isCancelledFalse, lấy tất cả đơn hàng
        return orderRepository.findByCustomerCustomerID(customer.getCustomerID(), pageable);
    }
}