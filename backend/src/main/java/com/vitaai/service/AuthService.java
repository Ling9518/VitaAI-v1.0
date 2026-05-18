package com.vitaai.service;

import com.vitaai.dto.LoginRequest;
import com.vitaai.dto.RegisterRequest;
import com.vitaai.entity.User;
import com.vitaai.repository.UserRepository;
import com.vitaai.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    private final SecureRandom secureRandom = new SecureRandom();

    private final Map<String, VerificationCode> codeStore = new HashMap<>();

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("两次密码不一致");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已注册");
        }

        // 验证验证码
        VerificationCode vc = codeStore.get(request.getEmail());
        if (vc == null || vc.isExpired() || !vc.code.equals(request.getCaptchaCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }
        codeStore.remove(request.getEmail());

        User.Role role = User.Role.USER;
        if ("DOCTOR".equalsIgnoreCase(request.getRole())) {
            role = User.Role.DOCTOR;
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .realName(request.getRealName())
                .gender(User.Gender.UNKNOWN)
                .doctorLicense(request.getDoctorLicense())
                .doctorDept(request.getDoctorDept())
                .doctorTitle(request.getDoctorTitle())
                .isVerified(true)
                .build();
        userRepository.save(user);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("role", user.getRole().name());
        result.put("emailVerified", true);
        return result;
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByUsernameOrEmail(request.getAccount(), request.getAccount())
                .orElseThrow(() -> new RuntimeException("账号或密码错误"));

        if (user.getIsDisabled()) {
            throw new RuntimeException("账户已被禁用");
        }

        // 检查锁定状态
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("账户已锁定，请15分钟后再试");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            Integer current = user.getLoginAttempts();
            int attempts = (current == null ? 0 : current) + 1;
            user.setLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                user.setLoginAttempts(0);
                userRepository.save(user);
                throw new RuntimeException("连续登录失败5次，账户已锁定15分钟");
            }
            userRepository.save(user);
            throw new RuntimeException("账号或密码错误");
        }

        // 登录成功
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("expiresIn", 7200);
        data.put("tokenType", "Bearer");

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole().name());
        userInfo.put("avatarUrl", user.getAvatarUrl());
        userInfo.put("realName", user.getRealName());
        data.put("user", userInfo);

        return data;
    }

    public void sendVerificationCode(String email, String type) {
        String code = String.format("%06d", secureRandom.nextInt(999999));
        codeStore.put(email, new VerificationCode(code, LocalDateTime.now().plusMinutes(5)));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("VitaAI智慧医院 - 验证码");
        message.setText("您的验证码是：" + code + "\n有效期5分钟，请勿泄露给他人。\n\n如非本人操作，请忽略此邮件。");
        mailSender.send(message);
    }

    public void verifyEmail(String email, String code) {
        VerificationCode vc = codeStore.get(email);
        if (vc == null || vc.isExpired() || !vc.code.equals(code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        codeStore.remove(email);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("两次密码不一致");
        }
        VerificationCode vc = codeStore.get(email);
        if (vc == null || vc.isExpired() || !vc.code.equals(code)) {
            throw new RuntimeException("验证码错误或已过期");
        }
        codeStore.remove(email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("该邮箱未注册"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Token已过期，请重新登录");
        }
        Long userId = jwtService.getUserId(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String newAccessToken = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", newAccessToken);
        data.put("refreshToken", newRefreshToken);
        data.put("expiresIn", 7200);
        data.put("tokenType", "Bearer");
        return data;
    }

    private static class VerificationCode {
        String code;
        LocalDateTime expiresAt;

        VerificationCode(String code, LocalDateTime expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }
}
