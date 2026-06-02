package com.finance.manager.security;

import com.finance.manager.entity.User;
import com.finance.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Used by Spring's AuthenticationManager (login/register flow).
     * Username here is the user's email.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return buildUserDetails(user);
    }

    /**
     * Used by JwtAuthenticationFilter to resolve a user by their immutable UUID.
     * Also exposes the raw User entity for tokenVersion comparison.
     */
    @Transactional
    public User loadUserEntityById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
    }

    public static UserDetails buildUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getId().toString(),   // principal name = UUID string
                user.getPasswordHash(),
                new ArrayList<>()
        );
    }

}
