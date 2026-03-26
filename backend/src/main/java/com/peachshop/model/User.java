package com.peachshop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long userId;
    
    private String username;
    private String password;
    private String email;
    private String phone;
    private String nick;
    private String avatar;
    private String province;
    private String city;
    
    @Column(name = "is_valid")
    private Integer isValid = 1;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "score")
    private Integer score = 0;
    
    @Column(name = "balance")
    private Double balance = 0.0;
    
    @Column(name = "growth")
    private Integer growth = 0;
    
    // Getters and Setters
    public Long getId() {
        return userId;
    }
    
    public void setId(Long id) {
        this.userId = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getNick() {
        return nick;
    }
    
    public void setNick(String nick) {
        this.nick = nick;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
    }
    
    public Double getBalance() {
        return balance;
    }
    
    public void setBalance(Double balance) {
        this.balance = balance;
    }
    
    public Integer getGrowth() {
        return growth;
    }
    
    public void setGrowth(Integer growth) {
        this.growth = growth;
    }
    
    public Integer getIsValid() {
        return isValid;
    }
    
    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }
}