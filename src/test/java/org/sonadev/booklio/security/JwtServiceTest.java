package org.sonadev.booklio.security;

import org.junit.jupiter.api.Test;
import org.sonadev.booklio.config.JwtService;
import org.sonadev.booklio.model.Role;
import org.sonadev.booklio.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldGenerateToken() {
        User user = new User();

        user.setEmail("sona@gmail.com");
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);

        assertNotNull(token);
    }

    @Test
    void shouldExtractEmailFromToken() {
        User user = new User();

        user.setEmail("sona@gmail.com");
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);

        String email = jwtService.extractEmail(token);

        assertEquals("sona@gmail.com", email);
    }

    @Test
    void shouldValidateToken() {
        User user = new User();

        user.setEmail("sona@gmail.com");
        user.setRole(Role.USER);

        String token = jwtService.generateToken(user);

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                .builder()
                .username("sona@gmail.com")
                .password("ignored")
                .roles("USER")
                .build();

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

}
