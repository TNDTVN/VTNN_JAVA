package com.example.vtnn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cartitems")
public class CartItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartitemid")
    private int cartItemID;

    @Column(name = "productid")
    private int productID;

    @Column(name = "productname")
    private String productName;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "unitprice")
    private BigDecimal unitPrice;

    @Column(name = "imageurl")
    private String imageUrl;

    @Column(name = "customerid")
    private int customerID;
    public CartItems(int productID, String productName, int quantity, BigDecimal unitPrice, String imageUrl, int customerID) {
        this.productID = productID;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.imageUrl = imageUrl;
        this.customerID = customerID;
    }

    public CartItems() {
    }
    public void setProduct(Product product) {
        this.product = product;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public void setCartItemID(int cartItemID) {
        this.cartItemID = cartItemID;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Product getProduct() {
        return product;
    }

    public int getCustomerID() {
        return customerID;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getCartItemID() {
        return cartItemID;
    }

    public String getProductName() {
        return productName;
    }

    public int getProductID() {
        return productID;
    }

    @ManyToOne
    @JoinColumn(name = "customerID", insertable = false, updatable = false)
    @JsonManagedReference
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "productID", insertable = false, updatable = false)
    @JsonManagedReference
    private Product product;

    // Getters and Setters
}

