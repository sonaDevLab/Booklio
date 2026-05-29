package org.sonadev.booklio.service;

import lombok.AllArgsConstructor;
import org.sonadev.booklio.dto.RegisterRequest;
import org.sonadev.booklio.exception.InvalidReservationException;
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
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

}
