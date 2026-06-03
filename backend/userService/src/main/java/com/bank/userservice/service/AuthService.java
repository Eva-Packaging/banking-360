package com.bank.userservice.service;

import com.bank.userservice.dto.LoginResponse;
import com.bank.userservice.entity.User;
import com.bank.userservice.exception.ServerError;
import com.bank.userservice.exception.UnauthorizedResponseException;
import com.bank.userservice.repository.UserRepository;
import com.bank.userservice.security.JwtUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final Logger logger = LogManager.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(String email, String password) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedResponseException("Invalid credentials"));

            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new UnauthorizedResponseException("Invalid credentials");
            }

            String token = jwtUtil.generateToken(user.getEmail());

            return new LoginResponse(
                    token,
                    "Bearer",
                    jwtUtil.extractExpiration(token)
            );
        } catch (DataAccessException e) {
            logger.error(e.getMessage());
            throw new ServerError(e.getMessage());
        }
    }
}