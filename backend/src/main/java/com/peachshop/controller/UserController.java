package com.peachshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * 用户登录[手机号码]
     */
    @PostMapping("/m/login")
    public ResponseEntity<Map<String, Object>> userMLogin(
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "pwd", required = false) String pwd,
            @RequestParam(value = "token", required = false) String token) {
        System.out.println("=== UserController: userMLogin called with mobile: " + mobile + ", pwd: " + pwd);
        
        // 模拟登录成功，返回token
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", "mock_token_" + System.currentTimeMillis());
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 使用手机号码和验证码登录
     */
    @PostMapping("/m/loginMobile")
    public ResponseEntity<Map<String, Object>> userMLoginMobile(
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "code", required = false) String code) {
        System.out.println("=== UserController: userMLoginMobile called with mobile: " + mobile + ", code: " + code);
        
        // 模拟登录成功，返回token
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", "mock_token_" + System.currentTimeMillis());
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 用户注册[手机号]
     */
    @PostMapping("/m/register")
    public ResponseEntity<Map<String, Object>> userMRegister(
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "pwd", required = false) String pwd) {
        System.out.println("=== UserController: userMRegister called with mobile: " + mobile + ", code: " + code);
        
        // 模拟注册成功，返回token
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", "mock_token_" + System.currentTimeMillis());
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 重设登陆密码
     */
    @PostMapping("/m/reset-pwd")
    public ResponseEntity<Map<String, Object>> userMResetPwd(
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "pwd", required = false) String pwd) {
        System.out.println("=== UserController: userMResetPwd called with mobile: " + mobile + ", code: " + code);
        
        // 模拟重置密码成功
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", null);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 退出登录
     */
    @GetMapping("/loginout")
    public ResponseEntity<Map<String, Object>> userLoginOut() {
        System.out.println("=== UserController: userLoginOut called");
        
        // 模拟退出登录成功
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", null);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 查看用户详情
     */
    @GetMapping("/detail")
    public ResponseEntity<Map<String, Object>> userDetail() {
        System.out.println("=== UserController: userDetail called");
        
        // 模拟用户详情
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> base = new HashMap<>();
        base.put("id", 1);
        base.put("user_id", 1);
        base.put("nick", "测试用户");
        base.put("avatar", "");
        data.put("base", base);
        
        Map<String, Object> userLevel = new HashMap<>();
        data.put("userLevel", userLevel);
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 修改用户信息
     */
    @GetMapping("/modify")
    public ResponseEntity<Map<String, Object>> userModify() {
        System.out.println("=== UserController: userModify called");
        
        // 模拟修改用户信息成功
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", null);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 查看用户资产
     */
    @GetMapping("/amount")
    public ResponseEntity<Map<String, Object>> userAmount() {
        System.out.println("=== UserController: userAmount called");
        
        // 模拟用户资产
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        
        Map<String, Object> data = new HashMap<>();
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 绑定手机号码
     */
    @PostMapping("/m/bind-mobile")
    public ResponseEntity<Map<String, Object>> userMBindMobile(
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "code", required = false) String code) {
        System.out.println("=== UserController: userMBindMobile called with mobile: " + mobile + ", code: " + code);
        
        // 模拟绑定手机成功
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", null);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 修改登陆密码
     */
    @PostMapping("/modify/password")
    public ResponseEntity<Map<String, Object>> userModifyPwd(
            @RequestParam(value = "oldPwd", required = false) String oldPwd,
            @RequestParam(value = "newPwd", required = false) String newPwd) {
        System.out.println("=== UserController: userModifyPwd called with oldPwd: " + oldPwd + ", newPwd: " + newPwd);
        
        // 模拟修改密码成功
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        response.put("data", null);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 资金流水
     */
    @PostMapping("/cashLog/v2")
    public ResponseEntity<Map<String, Object>> userCashLog(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        System.out.println("=== UserController: userCashLog called with page: " + page + ", pageSize: " + pageSize);
        
        // 模拟资金流水
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "success");
        
        Map<String, Object> data = new HashMap<>();
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }
}
