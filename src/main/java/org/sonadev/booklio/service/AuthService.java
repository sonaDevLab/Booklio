package org.sonadev.booklio.service;

import lombok.AllArgsConstructor;
import org.sonadev.booklio.config.JwtService;
import org.sonadev.booklio.dto.AuthResponse;
import org.sonadev.booklio.dto.LoginRequest;
import org.sonadev.booklio.dto.RegisterRequest;
import org.sonadev.booklio.exception.InvalidReservationException;
import org.sonadev.booklio.exception.ResourceNotFoundException;
import org.sonadev.booklio.model.Role;
import org.sonadev.booklio.model.User;
import org.sonadev.booklio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final JwtService jwtService;

    // Register User
    public void register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidReservationException("Email already in use");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(Role.USER);

        userRepository.save(user);
    }

    // Login
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials"));

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!matches) {
            throw new ResourceNotFoundException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }

}
