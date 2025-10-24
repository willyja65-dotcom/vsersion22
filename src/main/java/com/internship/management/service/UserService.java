package com.internship.management.service;

import com.internship.management.dto.MessageCode;
import com.internship.management.dto.UserDTO;
import com.internship.management.entity.User;
import com.internship.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String UPLOAD_DIR = "frontend/public/uploads/profile-images/";
    private static final String CV_UPLOAD_DIR = "frontend/public/uploads/cv-files/";

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(MessageCode.USER_NOT_FOUND.name()));
        return UserDTO.fromEntity(user);
    }

    @Transactional
    public UserDTO updateUserProfile(String email, UserDTO updateRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(MessageCode.USER_NOT_FOUND.name()));

        if (updateRequest.getNom() != null) {
            user.setNom(updateRequest.getNom());
        }
        if (updateRequest.getPrenom() != null) {
            user.setPrenom(updateRequest.getPrenom());
        }
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getDepartment() != null) {
            user.setDepartment(updateRequest.getDepartment());
        }
        if (updateRequest.getAvatar() != null) {
            user.setAvatar(updateRequest.getAvatar());
        }
        if (updateRequest.getDateNaissance() != null) {
            user.setDateNaissance(updateRequest.getDateNaissance());
        }
        if (updateRequest.getCvPath() != null) {
            user.setCvPath(updateRequest.getCvPath());
        }

        user = userRepository.save(user);
        return UserDTO.fromEntity(user);
    }

    @Transactional
    public String uploadAvatar(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(MessageCode.USER_NOT_FOUND.name()));

        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Le fichier doit être une image");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = UUID.randomUUID().toString() + fileExtension;
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            String avatarUrl = "/uploads/profile-images/" + fileName;
            user.setAvatar(avatarUrl);
            userRepository.save(user);

            return avatarUrl;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload du fichier: " + e.getMessage());
        }
    }

    @Transactional
    public String uploadCV(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(MessageCode.USER_NOT_FOUND.name()));

        if (file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new RuntimeException("Le fichier doit être un PDF");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String fileName = UUID.randomUUID().toString() + ".pdf";
            Path uploadPath = Paths.get(CV_UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            String cvUrl = "/uploads/cv-files/" + fileName;
            user.setCvPath(cvUrl);
            userRepository.save(user);

            return cvUrl;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload du CV: " + e.getMessage());
        }
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(MessageCode.USER_NOT_FOUND.name()));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Le mot de passe actuel est incorrect");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("Le nouveau mot de passe doit contenir au moins 8 caractères");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserDTO suspendAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(MessageCode.USER_NOT_FOUND.name()));

        user.setAccountStatus(User.AccountStatus.SUSPENDED);
        user = userRepository.save(user);
        return UserDTO.fromEntity(user);
    }

    @Transactional
    public UserDTO activateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(MessageCode.USER_NOT_FOUND.name()));

        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user = userRepository.save(user);
        return UserDTO.fromEntity(user);
    }
}
