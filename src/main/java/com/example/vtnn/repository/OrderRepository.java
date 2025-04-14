package com.example.vtnn.repository;

import com.example.vtnn.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    boolean existsByEmployeeEmployeeID(int employeeID);
    boolean existsByCustomerCustomerID(int customerID);

    // Cho hóa đơn đã duyệt
    Page<Order> findByEmployeeIDNotNullAndIsCancelledFalse(Pageable pageable);
    Page<Order> findByEmployeeEmployeeIDAndEmployeeIDNotNullAndIsCancelledFalse(Integer employeeID, Pageable pageable);
    Page<Order> findByCustomerCustomerIDAndEmployeeIDNotNullAndIsCancelledFalse(Integer customerID, Pageable pageable);
    Page<Order> findByCustomerCustomerNameContainingOrEmployeeFirstNameContainingAndEmployeeIDNotNullAndIsCancelledFalse(
            String customerName, String employeeName, Pageable pageable);

    @Query(value = "SELECT o.* FROM ordertable o " +
            "LEFT JOIN orderdetail od ON o.orderid = od.orderid " +
            "WHERE o.employeeid IS NOT NULL AND o.is_cancelled = false " +
            "GROUP BY o.orderid " +
            "ORDER BY COALESCE(SUM(od.unitprice * od.quantity * (1 - od.discount)), 0) ASC",
            nativeQuery = true)
    Page<Order> findAllSortedByTotalAmountAsc(Pageable pageable);

    @Query(value = "SELECT o.* FROM ordertable o " +
            "LEFT JOIN orderdetail od ON o.orderid = od.orderid " +
            "WHERE o.employeeid IS NOT NULL AND o.is_cancelled = false " +
            "GROUP BY o.orderid " +
            "ORDER BY COALESCE(SUM(od.unitprice * od.quantity * (1 - od.discount)), 0) DESC",
            nativeQuery = true)
    Page<Order> findAllSortedByTotalAmountDesc(Pageable pageable);

    // Cho hóa đơn chưa duyệt
    Page<Order> findByEmployeeIDIsNullAndIsCancelledFalse(Pageable pageable);
    Page<Order> findByCustomerCustomerIDAndEmployeeIDIsNullAndIsCancelledFalse(Integer customerID, Pageable pageable);
    Page<Order> findByCustomerCustomerNameContainingAndEmployeeIDIsNullAndIsCancelledFalse(String customerName, Pageable pageable);

    // Cho hóa đơn đã hủy
    Page<Order> findByIsCancelledTrue(Pageable pageable);
    Page<Order> findByEmployeeEmployeeIDAndIsCancelledTrue(Integer employeeID, Pageable pageable);
    Page<Order> findByCustomerCustomerIDAndIsCancelledTrue(Integer customerID, Pageable pageable);
    Page<Order> findByCustomerCustomerIDAndEmployeeEmployeeIDAndIsCancelledTrue(Integer customerID, Integer employeeID, Pageable pageable);
    Page<Order> findByCustomerCustomerNameContainingAndEmployeeEmployeeIDAndIsCancelledTrue(String customerName, Integer employeeID, Pageable pageable);
    Page<Order> findByCustomerCustomerNameContainingOrEmployeeFirstNameContainingAndIsCancelledTrue(
            String customerName, String employeeName, Pageable pageable);

    @Query(value = "SELECT o.* FROM ordertable o " +
            "LEFT JOIN orderdetail od ON o.orderid = od.orderid " +
            "WHERE o.employeeid = :employeeID AND o.employeeid IS NOT NULL AND o.iscancelled = false " +
            "GROUP BY o.orderid " +
            "ORDER BY COALESCE(SUM(od.unitprice * od.quantity * (1 - od.discount)), 0) ASC",
            nativeQuery = true)
    Page<Order> findByEmployeeEmployeeIDAndEmployeeIDNotNullAndIsCancelledFalseSortedByTotalAmountAsc(
            @Param("employeeID") Integer employeeID, Pageable pageable);

    @Query(value = "SELECT o.* FROM ordertable o " +
            "LEFT JOIN orderdetail od ON o.orderid = od.orderid " +
            "WHERE o.employeeid = :employeeID AND o.employeeid IS NOT NULL AND o.iscancelled = false " +
            "GROUP BY o.orderid " +
            "ORDER BY COALESCE(SUM(od.unitprice * od.quantity * (1 - od.discount)), 0) DESC",
            nativeQuery = true)
    Page<Order> findByEmployeeEmployeeIDAndEmployeeIDNotNullAndIsCancelledFalseSortedByTotalAmountDesc(
            @Param("employeeID") Integer employeeID, Pageable pageable);

    List<Order> findByIsCancelledFalseAndEmployeeIDNotNull();

    // Lấy hóa đơn đã duyệt của một nhân viên cụ thể
    @Query("SELECT o FROM Order o WHERE o.employeeID = :employeeID AND o.isCancelled = false AND o.employeeID IS NOT NULL")
    List<Order> findByEmployeeIDAndIsCancelledFalseAndEmployeeIDNotNull(@Param("employeeID") Integer employeeID);

    List<Order> findByCustomerCustomerIDAndIsCancelledFalse(int customerID);
    Page<Order> findByCustomerCustomerID(Integer customerID, Pageable pageable);
    long countByIsCancelledFalseAndEmployeeIDIsNotNull();
    @Query("SELECT e.firstName, e.lastName, e.email, " +
            "COALESCE(SUM(od.unitPrice * od.quantity * (1 - od.discount)), 0) " +
            "FROM Order o " +
            "JOIN o.employee e " +
            "LEFT JOIN o.orderDetails od " +
            "WHERE o.employeeID IS NOT NULL " +
            "AND o.isCancelled = false " +
            "AND (:year = -1 OR YEAR(o.orderDate) = :year) " +
            "AND (:month = -1 OR MONTH(o.orderDate) = :month) " +
            "GROUP BY e.firstName, e.lastName, e.email, o.orderID")
    List<Object[]> findApprovedOrdersWithEmployeeDetailsAndNotCancelled(int year, int month);
}