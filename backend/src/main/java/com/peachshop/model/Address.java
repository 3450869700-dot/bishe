package com.peachshop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_address")
public class Address {

    public Address() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "link_man")
    private String linkMan;
    
    @Column(name = "mobile")
    private String mobile;
    
    @Column(name = "province_id")
    private String provinceId;
    
    @Column(name = "province_str")
    private String provinceStr;
    
    @Column(name = "city_id")
    private String cityId;
    
    @Column(name = "city_str")
    private String cityStr;
    
    @Column(name = "district_id")
    private String districtId;
    
    @Column(name = "area_str")
    private String areaStr;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "is_default")
    private Integer isDefault;
    
    @Column(name = "is_valid")
    private Integer isValid = 1;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLinkMan() {
        return linkMan;
    }

    public void setLinkMan(String linkMan) {
        this.linkMan = linkMan;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }

    public String getProvinceStr() {
        return provinceStr;
    }

    public void setProvinceStr(String provinceStr) {
        this.provinceStr = provinceStr;
    }

    public String getCityId() {
        return cityId;
    }

    public void setCityId(String cityId) {
        this.cityId = cityId;
    }

    public String getCityStr() {
        return cityStr;
    }

    public void setCityStr(String cityStr) {
        this.cityStr = cityStr;
    }

    public String getDistrictId() {
        return districtId;
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getAreaStr() {
        return areaStr;
    }

    public void setAreaStr(String areaStr) {
        this.areaStr = areaStr;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
