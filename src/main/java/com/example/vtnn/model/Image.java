package com.example.vtnn.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

@Entity
@Table(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int imageID;

    @Column(name = "productid")
    private int productID;

    @Column(name = "imagename")
    private String imageName;

    @ManyToOne
    @JoinColumn(name = "productID", insertable = false, updatable = false)
    @JsonManagedReference
    private Product product;
    public Image(int productID, String imageName) {
        this.productID = productID;
        this.imageName = imageName;
    }

    public Image() {
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public int getImageID() {
        return imageID;
    }

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

}
