package org.sonadev.booklio.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sonadev.booklio.dto.RegisterRequest;
import org.sonadev.booklio.exception.InvalidReservationException;
import org.sonadev.booklio.model.User;
import org.sonadev.booklio.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

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
}
