package com.niit.memory.controller;

import com.niit.memory.config.JwtUtil;
import com.niit.memory.config.Result;
import com.niit.memory.config.UserContext;
import com.niit.memory.entity.User;
import com.niit.memory.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil,
                          BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            log.info("Login attempt: username={}", username);
            User user = userRepository.findByUsername(username);
            if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
                log.warn("Login failed: username={}, userExists={}", username, user != null);
                return Result.error("用户名或密码错误");
            }
            String token = jwtUtil.generateToken(user.getId(), user.getUsername());
            log.info("Login success: username={}, userId={}", username, user.getId());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userId", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("avatarUrl", user.getAvatarUrl());
            return Result.success(data);
        } catch (Exception e) {
            log.error("Login error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/me")
    public Result getCurrentUser() {
        try {
            Long userId = UserContext.getUserId();
            User user = userRepository.findById(userId);
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("username", user.getUsername());
            data.put("nickname", user.getNickname());
            data.put("avatarUrl", user.getAvatarUrl());
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/user/{id}")
    public Result getUserById(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id);
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("nickname", user.getNickname());
            data.put("avatarUrl", user.getAvatarUrl());
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/avatars")
    public Result getAvatars() {
        try {
            User user1 = userRepository.findById(1L);
            User user2 = userRepository.findById(2L);
            Map<String, Object> data = new HashMap<>();
            data.put("hisNickname", user1 != null ? user1.getNickname() : "他");
            data.put("herNickname", user2 != null ? user2.getNickname() : "她");
            data.put("hisAvatarUrl", user1 != null ? user1.getAvatarUrl() : null);
            data.put("herAvatarUrl", user2 != null ? user2.getAvatarUrl() : null);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/avatar")
    public Result updateAvatar(@RequestBody Map<String, String> body) {
        try {
            Long userId = UserContext.getUserId();
            String avatarUrl = body.get("avatarUrl");
            userRepository.updateAvatar(userId, avatarUrl);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
