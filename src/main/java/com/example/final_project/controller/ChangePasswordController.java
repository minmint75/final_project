package com.example.final_project.controller;

import com.example.final_project.dto.ChangePasswordRequest;
import com.example.final_project.service.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChangePasswordController {

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        try {
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty() ||
                request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                response.put("message", "Vui lòng nhập đầy đủ thông tin");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getNewPassword().length() < 6) {
                response.put("message", "Mật khẩu phải có ít nhất 6 ký tự");
                return ResponseEntity.badRequest().body(response);
            }

            passwordService.changePassword(request, authentication);

            response.put("message", "Đổi mật khẩu thành công");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(401).body(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Đã xảy ra lỗi khi đổi mật khẩu");
            return ResponseEntity.status(500).body(response);
        }
    }
}
