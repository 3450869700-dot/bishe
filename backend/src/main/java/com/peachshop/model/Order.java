package com.peachshop.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "order_number")
    private String orderNumber;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "status")
    private Integer status;
    
    @Column(name = "status_str")
    private String statusStr;
    
    @Column(name = "amount")
    private Double amount;
    
    @Column(name = "amount_real")
    private Double amountReal;
    
    @Column(name = "amount_logistics")
    private Double amountLogistics;
    
    @Column(name = "is_need_logistics")
    private Integer isNeedLogistics;
    
    @Column(name = "remark")
    private String remark;
    
    @Column(name = "date_add")
    private LocalDateTime dateAdd;
    
    @Column(name = "date_close")
    private LocalDateTime dateClose;
    
    @Column(name = "payment_time")
    private LocalDateTime paymentTime;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "trace_code")
    private String traceCode;
    
    @Column(name = "link_man")
    private String linkMan;
    
    @Column(name = "mobile")
    private String mobile;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "province_id")
    private String provinceId;
    
    @Column(name = "city_id")
    private String cityId;
    
    @Column(name = "district_id")
    private String districtId;
    
    @Column(name = "is_valid")
    private Integer isValid = 1;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getStatusStr() {
        return statusStr;
    }
    
    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public Double getAmountReal() {
        return amountReal;
    }
    
    public void setAmountReal(Double amountReal) {
        this.amountReal = amountReal;
    }
    
    public Double getAmountLogistics() {
        return amountLogistics;
    }
    
    public void setAmountLogistics(Double amountLogistics) {
        this.amountLogistics = amountLogistics;
    }
    
    public Integer getIsNeedLogistics() {
        return isNeedLogistics;
    }
    
    public void setIsNeedLogistics(Integer isNeedLogistics) {
        this.isNeedLogistics = isNeedLogistics;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public LocalDateTime getDateAdd() {
        return dateAdd;
    }
    
    public void setDateAdd(LocalDateTime dateAdd) {
        this.dateAdd = dateAdd;
    }
    
    public LocalDateTime getDateClose() {
        return dateClose;
    }
    
    public void setDateClose(LocalDateTime dateClose) {
        this.dateClose = dateClose;
    }
    
    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }
    
    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getTraceCode() {
        return traceCode;
    }
    
    public void setTraceCode(String traceCode) {
        this.traceCode = traceCode;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getProvinceId() {
        return provinceId;
    }
    
    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }
    
    public String getCityId() {
        return cityId;
    }
    
    public void setCityId(String cityId) {
        this.cityId = cityId;
    }
    
    public String getDistrictId() {
        return districtId;
    }
    
    public void setDistrictId(String districtId) {
        this.districtId = districtId;
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
