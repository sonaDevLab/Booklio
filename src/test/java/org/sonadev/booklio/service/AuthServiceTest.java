package org.sonadev.booklio.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sonadev.booklio.config.JwtService;
import org.sonadev.booklio.dto.AuthResponse;
import org.sonadev.booklio.dto.LoginRequest;
import org.sonadev.booklio.dto.RegisterRequest;
import org.sonadev.booklio.exception.InvalidReservationException;
import org.sonadev.booklio.exception.ResourceNotFoundException;
import org.sonadev.booklio.model.User;
import org.sonadev.booklio.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    /* REGISTER */
    @Test
    void shouldRegisterUserSuccessfully(){
        RegisterRequest request = new RegisterRequest();

        request.setName("Sona");
        request.setEmail("sona@gmail.com");
        request.setPassword("1234");

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("hashed-password");

        authService.register(request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();

        request.setEmail("sona@gmail.com");

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        assertThrows(
                InvalidReservationException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any());
    }

    /* LOGIN */
    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest();

        request.setEmail("sona@gmail.com");
        request.setPassword("123456");

        User user = new User();

        user.setEmail("sona@gmail.com");
        user.setPassword("hashed-password");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("123456", "hashed-password"))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("fake-jwt");

        AuthResponse response = authService.login(request);

        assertEquals("fake-jwt", response.getToken());

    }

    @Test
    void shouldThrowExceptionWhenPasswoordIsWrong() {
        LoginRequest request = new LoginRequest();

        request.setEmail("sona@gmail.com");
        request.setPassword("wrong");

        User user = new User();

        user.setPassword(passwordEncoder.encode("123456"));

        when(userRepository.findByEmail(any()))
                .thenReturn(Optional.of(user));

        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.login(request)
        );

    }
}
