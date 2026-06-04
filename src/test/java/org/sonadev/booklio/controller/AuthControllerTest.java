package org.sonadev.booklio.controller;

import org.junit.jupiter.api.Test;
import org.sonadev.booklio.exception.InvalidReservationException;
import org.sonadev.booklio.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType("application/json")
                .content("""
               {
                    "name": "Sona",
                    "email": "sona@gmail.com",
                    "password": "123456"
               }
               """))
                .andExpect(status().isCreated());

        verify(authService).register(any());
    }

    @Test
    void shouldReturnBadRequestWhenEmailAlreadyExists() throws Exception {
        doThrow(new InvalidReservationException("Email already in use"))
                .when(authService).register(any());

        mockMvc.perform(post("/auth/register")
                .contentType("application/json")
                .content("""
               {
                    "name": "Sona",
                    "email": "sona@gmail.com",
                    "password": "123456"
               }
               """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Email already in use"));
    }

}
