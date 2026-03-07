package com.gestionespese.controller;

import com.gestionespese.dto.user.PasswordUpdateDto;
import com.gestionespese.dto.user.UserProfile;
import com.gestionespese.dto.user.UserUpdateDto;
import com.gestionespese.model.User;
import com.gestionespese.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> me(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(new UserProfile(
                user.getId().toString(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                java.time.OffsetDateTime.now() // TODO: Add createdAt to User or use now
        ));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfile> updateProfile(@RequestBody UserUpdateDto dto, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.firstName() != null)
            user.setFirstName(dto.firstName());
        if (dto.lastName() != null)
            user.setLastName(dto.lastName());

        userRepository.save(user);

        return ResponseEntity.ok(new UserProfile(
                user.getId().toString(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                java.time.OffsetDateTime.now()));
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordUpdateDto dto, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().build();
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
