package com.internship.management.controller;

import com.internship.management.dto.ApiResponse;
import com.internship.management.dto.MessageCode;
import com.internship.management.dto.UserDTO;
import com.internship.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUserProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserDTO user = userService.getUserByEmail(email);
            return ResponseEntity.ok(ApiResponse.success(MessageCode.USER_FOUND.name(), user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            Authentication authentication,
            @RequestBody UserDTO updateRequest) {
        try {
            String email = authentication.getName();
            UserDTO updatedUser = userService.updateUserProfile(email, updateRequest);
            return ResponseEntity.ok(ApiResponse.success(MessageCode.USER_UPDATED.name(), updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/profile/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        try {
            String email = authentication.getName();
            String avatarPath = userService.uploadAvatar(email, file);
            return ResponseEntity.ok(ApiResponse.success(MessageCode.AVATAR_UPLOADED.name(), avatarPath));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/profile/cv")
    public ResponseEntity<ApiResponse<String>> uploadCV(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {
        try {
            String email = authentication.getName();
            String cvPath = userService.uploadCV(email, file);
            return ResponseEntity.ok(ApiResponse.success("CV_UPLOADED", cvPath));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request) {
        try {
            String email = authentication.getName();
            userService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("PASSWORD_CHANGED", "Mot de passe modifié avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{userId}/suspend")
    public ResponseEntity<ApiResponse<UserDTO>> suspendAccount(@PathVariable Long userId) {
        try {
            UserDTO user = userService.suspendAccount(userId);
            return ResponseEntity.ok(ApiResponse.success("ACCOUNT_SUSPENDED", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<UserDTO>> activateAccount(@PathVariable Long userId) {
        try {
            UserDTO user = userService.activateAccount(userId);
            return ResponseEntity.ok(ApiResponse.success("ACCOUNT_ACTIVATED", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
