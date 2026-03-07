package com.gestionespese.service;

import com.gestionespese.dto.auth.AuthResponse;
import com.gestionespese.dto.auth.LoginRequest;
import com.gestionespese.dto.auth.RegisterRequest;
import com.gestionespese.model.User;
import com.gestionespese.repository.UserRepository;
import com.gestionespese.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        logger.info("Attempting registration for email: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            logger.warn("Registration failed: Email {} already exists", request.email());
            throw new RuntimeException("Email already exists");
        }

        var user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.firstName(),
                request.lastName());

        userRepository.save(user);
        logger.info("User registered successfully: {}", request.email());
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.of(jwtToken, "refresh-token-not-implemented-yet");
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("Attempting login for email: {}", request.email());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()));
        } catch (Exception e) {
            logger.error("Login failed for email: {} - {}", request.email(), e.getMessage());
            throw e;
        }

        var user = userRepository.findByEmail(request.email())
                .orElseThrow();

        logger.info("Login successful for email: {}", request.email());
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.of(jwtToken, "refresh-token-not-implemented-yet");
    }
}
