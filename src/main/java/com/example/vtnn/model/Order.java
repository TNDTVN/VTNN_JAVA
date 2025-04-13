package com.example.vtnn.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ordertable")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderID;

    @Column(name = "customerid")
    private Integer customerID;

    @Column(name = "shipaddress")
    private String shipAddress;

    @Column(name = "shipcity")
    private String shipCity;

    @Column(name = "shippostalcode")
    private String shipPostalCode;

    @Column(name = "shipcountry")
    private String shipCountry;

    @Column(name = "notes")
    private String notes;

    @Column(name = "freight")
    private BigDecimal freight;

    @Column(name = "employeeid")
    private Integer employeeID;

    @Column(name = "orderdate")
    private Date orderDate;

    @Column(name = "shippeddate")
    private Date shippedDate;

    @Column(name = "iscancelled", nullable = false, columnDefinition = "boolean default false")
    private boolean isCancelled = false;

    public boolean isCancelled() {
        return isCancelled;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public Integer getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(Integer employeeID) {
        this.employeeID = employeeID;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getShippedDate() {
        return shippedDate;
    }

    public void setShippedDate(Date shippedDate) {
        this.shippedDate = shippedDate;
    }

    public String getShipAddress() {
        return shipAddress;
    }

    public void setShipAddress(String shipAddress) {
        this.shipAddress = shipAddress;
    }

    public String getShipCity() {
        return shipCity;
    }

    public void setShipCity(String shipCity) {
        this.shipCity = shipCity;
    }

    public String getShipPostalCode() {
        return shipPostalCode;
    }

    public void setShipPostalCode(String shipPostalCode) {
        this.shipPostalCode = shipPostalCode;
    }

    public String getShipCountry() {
        return shipCountry;
    }

    public void setShipCountry(String shipCountry) {
        this.shipCountry = shipCountry;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public BigDecimal getFreight() {
        return freight;
    }

    public void setFreight(BigDecimal freight) {
        this.freight = freight;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Set<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(Set<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }


    @ManyToOne
    @JoinColumn(name = "customerID", insertable = false, updatable = false)
    @JsonManagedReference
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "employeeID", insertable = false, updatable = false)
    @JsonManagedReference
    private Employee employee;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonBackReference
    private Set<OrderDetail> orderDetails = new HashSet<>(); // Khởi tạo ngay tại đây

    public Order() {
        this.orderDetails = new HashSet<>(); // Hoặc khởi tạo trong constructor
    }

    public Order(Integer customerID, Integer employeeID, Date orderDate, Date shippedDate, String shipAddress, String shipCity, String shipPostalCode, String shipCountry, String notes, BigDecimal freight) {
        this.customerID = customerID;
        this.employeeID = employeeID;
        this.orderDate = orderDate;
        this.shippedDate = shippedDate;
        this.shipAddress = shipAddress;
        this.shipCity = shipCity;
        this.shipPostalCode = shipPostalCode;
        this.shipCountry = shipCountry;
        this.notes = notes;
        this.freight = freight;
        this.orderDetails = new HashSet<>();
    }

    public BigDecimal getTotalPrice() {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return orderDetails.stream()
                .map(detail -> {
                    return detail.getUnitPrice()
                            .multiply(BigDecimal.valueOf(detail.getQuantity()))
                            .multiply(BigDecimal.valueOf(1 - detail.getDiscount()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

