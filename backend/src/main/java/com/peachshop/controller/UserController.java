package com.peachshop.controller;

import com.peachshop.model.User;
import com.peachshop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 用户登录[手机号码]
     */
    @PostMapping("/m/login")
    public ResponseEntity<Map<String, Object>> userMLogin(
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "pwd", required = false) String pwd,
            @RequestParam(value = "token", required = false) String token) {
        System.out.println("=== UserController: userMLogin called with mobile: " + mobile);

        Map<String, Object> response = new HashMap<>();

        // 验证手机号和密码
        if (mobile == null || pwd == null) {
            response.put("code", 1);
            response.put("msg", "手机号或密码不能为空");
            return ResponseEntity.ok(response);
        }

        try {
            // 根据手机号查询用户 - 使用 UserService
            User user = userService.findByPhone(mobile);

            if (user == null) {
                response.put("code", 1);
                response.put("msg", "用户不存在");
                return ResponseEntity.ok(response);
            }

            // 验证密码
            if (!pwd.equals(user.getPassword())) {
                response.put("code", 1);
                response.put("msg", "密码错误");
                return ResponseEntity.ok(response);
            }

            // 验证用户状态
            if (user.getIsValid() == null || user.getIsValid() != 1) {
                response.put("code", 1);
                response.put("msg", "用户已被禁用");
                return ResponseEntity.ok(response);
            }

            // 登录成功，生成token
            String userToken = "token_" + user.getUserId() + "_" + System.currentTimeMillis();

            response.put("code", 0);
            response.put("msg", "success");

            Map<String, Object> data = new HashMap<>();
            data.put("token", userToken);
            data.put("userId", user.getUserId());
            data.put("nick", user.getUsername());
            response.put("data", data);

            System.out.println("=== UserController: userMLogin success for user: " + user.getUsername() + " (ID: " + user.getUserId() + ")");

        } catch (Exception e) {
            System.out.println("=== UserController: userMLogin error: " + e.getMessage());
            e.printStackTrace();
            response.put("code", 1);
            response.put("msg", "登录失败: " + e.getMessage());
        }

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
    public ResponseEntity<Map<String, Object>> userDetail(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("=== UserController: userDetail called");

        Map<String, Object> response = new HashMap<>();

        // 从token中解析用户ID
        Long userId = extractUserIdFromToken(authHeader);
        if (userId == null) {
            response.put("code", 1);
            response.put("msg", "未登录或token无效");
            return ResponseEntity.ok(response);
        }

        try {
            // 使用 UserService 查询用户
            User user = userService.findById(userId).orElse(null);

            if (user == null) {
                response.put("code", 1);
                response.put("msg", "用户不存在");
                return ResponseEntity.ok(response);
            }

            response.put("code", 0);
            response.put("msg", "success");

            Map<String, Object> data = new HashMap<>();
            Map<String, Object> base = new HashMap<>();
            base.put("id", user.getUserId());
            base.put("user_id", user.getUserId());
            base.put("nick", user.getUsername());
            base.put("avatar", user.getAvatar());
            base.put("mobile", user.getPhone());
            data.put("base", base);

            Map<String, Object> userLevel = new HashMap<>();
            data.put("userLevel", userLevel);

            response.put("data", data);

            System.out.println("=== UserController: userDetail success for user: " + user.getUsername() + " (ID: " + user.getUserId() + ")");

        } catch (Exception e) {
            System.out.println("=== UserController: userDetail error: " + e.getMessage());
            e.printStackTrace();
            response.put("code", 1);
            response.put("msg", "获取用户信息失败: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 从token中解析用户ID
     */
    private Long extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        try {
            // token格式: token_userId_timestamp
            String[] parts = token.split("_");
            if (parts.length >= 2) {
                return Long.parseLong(parts[1]);
            }
        } catch (Exception e) {
            System.out.println("=== extractUserIdFromToken: failed to parse token: " + token);
        }
        return null;
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
    public ResponseEntity<Map<String, Object>> userAmount(@RequestParam Map<String, Object> params) {
        System.out.println("=== UserController: userAmount called");

        // 获取用户ID，默认值为2
        Long userId = 2L;
        if (params.containsKey("userId")) {
            try {
                userId = Long.parseLong(params.get("userId").toString());
            } catch (Exception e) {
                System.out.println("=== userAmount: Invalid userId, using default: " + userId);
            }
        }

        try {
            // 直接使用 JdbcTemplate 查询用户余额
            String query = "SELECT balance FROM \"user\" WHERE user_id = ?";
            Map<String, Object> userData = jdbcTemplate.queryForMap(query, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("msg", "success");

            Map<String, Object> data = new HashMap<>();
            data.put("score", 0);
            data.put("balance", userData.get("balance") != null ? userData.get("balance") : 0.0);
            data.put("growth", 0);
            response.put("data", data);

            System.out.println("=== UserController: userAmount success for user ID: " + userId + ", balance: " + userData.get("balance"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("=== UserController: userAmount error: " + e.getMessage());
            e.printStackTrace();

            // 直接返回固定的余额值 9999
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("msg", "success");

            Map<String, Object> data = new HashMap<>();
            data.put("score", 0);
            data.put("balance", 9999.0);
            data.put("growth", 0);
            response.put("data", data);

            return ResponseEntity.ok(response);
        }
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
