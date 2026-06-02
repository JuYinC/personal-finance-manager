package com.finance.manager.service;

import com.finance.manager.dto.auth.AuthResponse;
import com.finance.manager.dto.user.ChangePasswordRequest;
import com.finance.manager.dto.user.UpdateUserRequest;
import com.finance.manager.dto.user.UserResponse;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.exception.UnauthorizedException;
import com.finance.manager.repository.UserRepository;
import com.finance.manager.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Resolves the authenticated user from the SecurityContext.
     * The principal name is a UUID string (set by JwtAuthenticationFilter).
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = UUID.fromString(authentication.getName());
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
    }

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return UUID.fromString(authentication.getName());
    }

    public UserResponse getCurrentUserProfile() {
        return mapToResponse(getCurrentUser());
    }

    @Transactional
    public UserResponse updateCurrentUser(UpdateUserRequest request) {
        User user = getCurrentUser();
        user.setName(request.getName());
        user = userRepository.save(user);
        return mapToResponse(user);
    }

    /**
     * Changes the user's password and immediately issues a fresh JWT so the
     * current session continues without interruption. tokenVersion is NOT
     * incremented — other sessions (other devices) remain valid. This is an
     * intentional trade-off: full multi-device revocation requires a logout.
     *
     * @return a new AuthResponse containing the fresh token
     */
    @Transactional
    public AuthResponse changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user = userRepository.save(user);

        // Issue a fresh token for the current session (seamless continuity).
        String newToken = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(newToken)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    // ---------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}
