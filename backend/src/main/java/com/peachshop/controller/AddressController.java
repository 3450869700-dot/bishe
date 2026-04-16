package com.peachshop.controller;

import com.peachshop.model.Address;
import com.peachshop.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /**
     * 获取收货地址列表
     */
    @PostMapping("/shipping-address/list/v2")
    public ResponseEntity<Map<String, Object>> getAddressList(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            response.put("code", 1);
            response.put("msg", "未登录或token无效");
            return ResponseEntity.ok(response);
        }

        try {
            List<Address> addresses = addressService.getAddressesByUserId(userId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", addresses);
            
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", data);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("=== AddressController: 获取地址列表失败: " + e.getMessage());
            e.printStackTrace();
            
            response.put("code", 500);
            response.put("msg", "获取地址列表失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取默认收货地址
     */
    @GetMapping("/shipping-address/default/v2")
    public ResponseEntity<Map<String, Object>> getDefaultAddress(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            response.put("code", 1);
            response.put("msg", "未登录或token无效");
            return ResponseEntity.ok(response);
        }

        try {
            Address address = addressService.getDefaultAddress(userId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("info", address);
            
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", data);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("=== AddressController: 获取默认地址失败: " + e.getMessage());
            e.printStackTrace();
            
            response.put("code", 500);
            response.put("msg", "获取默认地址失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取收货地址详情
     */
    @GetMapping("/shipping-address/detail/v2")
    public ResponseEntity<Map<String, Object>> getAddressDetail(
            @RequestParam Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            response.put("code", 1);
            response.put("msg", "未登录或token无效");
            return ResponseEntity.ok(response);
        }

        try {
            Address address = addressService.getAddressById(id);
            if (address == null) {
                response.put("code", 404);
                response.put("msg", "地址不存在");
                response.put("data", null);
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("info", address);
            
            response.put("code", 0);
            response.put("msg", "success");
            response.put("data", data);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("=== AddressController: 获取地址详情失败: " + e.getMessage());
            e.printStackTrace();
            
            response.put("code", 500);
            response.put("msg", "获取地址详情失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 添加收货地址
     */
    @PostMapping("/shipping-address/add")
    public ResponseEntity<Map<String, Object>> addAddress(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            response.put("code", 1);
            response.put("msg", "未登录或token无效");
            return ResponseEntity.ok(response);
        }

        try {
            Address address = new Address();
            address.setUserId(userId);
            address.setLinkMan((String) request.get("linkMan"));
            address.setMobile((String) request.get("mobile"));
            address.setProvinceId((String) request.get("provinceId"));
            address.setCityId((String) request.get("cityId"));
            address.setDistrictId((String) request.get("districtId"));
            address.setAddress((String) request.get("address"));
            
            // 解析地区字符串
            String areaStr = (String) request.get("areaStr");
            if (areaStr != null && !areaStr.isEmpty()) {
                String[] areas = areaStr.split("/");
                if (areas.length > 0) address.setProvinceStr(areas[0].trim());
                if (areas.length > 1) address.setCityStr(areas[1].trim());
                if (areas.length > 2) address.setAreaStr(areas[2].trim());
            }
            
            // 设置是否为默认地址
            Object isDefault = request.get("isDefault");
            if (isDefault != null) {
                address.setIsDefault(isDefault instanceof Boolean ? ((Boolean) isDefault ? 1 : 0) : Integer.parseInt(isDefault.toString()));
            } else {
                address.setIsDefault(0);
            }
            
            Address savedAddress = addressService.saveAddress(address);
            
            response.put("code", 0);
            response.put("msg", "添加成功");
            response.put("data", savedAddress);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("=== AddressController: 添加地址失败: " + e.getMessage());
            e.printStackTrace();
            
            response.put("code", 500);
            response.put("msg", "添加地址失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 修改收货地址
     */
    @PostMapping("/shipping-address/update")
    public ResponseEntity<Map<String, Object>> updateAddress(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            response.put("code", 1);
            response.put("msg", "未登录或token无效");
            return ResponseEntity.ok(response);
        }

        try {
            Long id = Long.valueOf(request.get("id").toString());
            Address address = addressService.getAddressById(id);
            
            if (address == null) {
                response.put("code", 404);
                response.put("msg", "地址不存在");
                response.put("data", null);
                return ResponseEntity.ok(response);
            }
            
            address.setLinkMan((String) request.get("linkMan"));
            address.setMobile((String) request.get("mobile"));
            address.setProvinceId((String) request.get("provinceId"));
            address.setCityId((String) request.get("cityId"));
            address.setDistrictId((String) request.get("districtId"));
            address.setAddress((String) request.get("address"));
            
            // 解析地区字符串
            String areaStr = (String) request.get("areaStr");
            if (areaStr != null && !areaStr.isEmpty()) {
                String[] areas = areaStr.split("/");
                if (areas.length > 0) address.setProvinceStr(areas[0].trim());
                if (areas.length > 1) address.setCityStr(areas[1].trim());
                if (areas.length > 2) address.setAreaStr(areas[2].trim());
            }
            
            // 设置是否为默认地址
            Object isDefault = request.get("isDefault");
            if (isDefault != null) {
                address.setIsDefault(isDefault instanceof Boolean ? ((Boolean) isDefault ? 1 : 0) : Integer.parseInt(isDefault.toString()));
            } else {
                address.setIsDefault(0);
            }
            
            Address savedAddress = addressService.saveAddress(address);
            
            response.put("code", 0);
            response.put("msg", "修改成功");
            response.put("data", savedAddress);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("=== AddressController: 修改地址失败: " + e.getMessage());
            e.printStackTrace();
            
            response.put("code", 500);
            response.put("msg", "修改地址失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除收货地址
     */
    @PostMapping("/shipping-address/delete")
    public ResponseEntity<Map<String, Object>> deleteAddress(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> response = new HashMap<>();
        
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            response.put("code", 1);
            response.put("msg", "未登录或token无效");
            return ResponseEntity.ok(response);
        }

        try {
            Long id = Long.valueOf(request.get("id").toString());
            addressService.deleteAddress(id);
            
            response.put("code", 0);
            response.put("msg", "删除成功");
            response.put("data", null);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("=== AddressController: 删除地址失败: " + e.getMessage());
            e.printStackTrace();
            
            response.put("code", 500);
            response.put("msg", "删除地址失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 从token中提取用户ID
     */
    private Long extractUserIdFromToken(String authHeader) {
        // 简化处理：万能账户返回固定ID
        return 2L;
    }
}
