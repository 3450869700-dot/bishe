package com.peachshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    public Product() {
    }

    @Id
    @Column(name = "product_code")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productCode;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "weight")
    private String weight;
    
    @Column(name = "packet")
    private String packet;
    
    @Column(name = "variety")
    private String variety;
    
    @Column(name = "grade")
    private String grade;
    
    @Column(name = "specification_desc")
    private String specName;
    
    @Column(name = "price")
    private String price;
    
    @Column(name = "order_quantity")
    private String orderQuantity;
    
    @Column(name = "heat")
    private String heat;
    
    @Column(name = "shop")
    private String shop;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "image")
    private String imageUrl;
    
    @Column(name = "rating")
    private Double rating;
    
    @Column(name = "stock")
    private Integer stock;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getPacket() {
        return packet;
    }

    public void setPacket(String packet) {
        this.packet = packet;
    }

    public String getVariety() {
        return variety;
    }

    public void setVariety(String variety) {
        this.variety = variety;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getSpecName() {
        return specName;
    }

    public void setSpecName(String specName) {
        this.specName = specName;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getOrderQuantity() {
        return orderQuantity;
    }

    public void setOrderQuantity(String orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public String getHeat() {
        return heat;
    }

    public void setHeat(String heat) {
        this.heat = heat;
    }

    public String getShop() {
        return shop;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getProductCode() {
        return productCode;
    }

    public void setProductCode(Long productCode) {
        this.productCode = productCode;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

}