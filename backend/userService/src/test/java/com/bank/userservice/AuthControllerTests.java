package com.bank.userservice;

import com.bank.userservice.controller.AuthController;
import com.bank.userservice.dto.LoginRequest;
import com.bank.userservice.dto.LoginResponse;
import com.bank.userservice.dto.RegisterCustomerRequest;
import com.bank.userservice.dto.RegisterCustomerResponse;
import com.bank.userservice.exception.EmailAlreadyExistsException;
import com.bank.userservice.service.AuthService;
import com.bank.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(AuthController.class)
class AuthControllerTests {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------
    // POST /auth/register - 201
    // ---------------------------
    @Test
    void register_ShouldReturn201_WhenSuccessful() throws Exception {

        RegisterCustomerRequest request = RegisterCustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("jdc1@web.co")
                .password("MyPassw0rd!")
                .build();

        RegisterCustomerResponse response = new RegisterCustomerResponse();
        response.setEmail("jdc1@web.co");

        when(userService.register(any(RegisterCustomerRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jdc1@web.co"));
    }

    // ---------------------------
    // POST /auth/register - 409
    // ---------------------------
    @Test
    void register_ShouldReturn409_WhenEmailAlreadyExists() throws Exception {

        RegisterCustomerRequest request = RegisterCustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("jdc1@web.co")
                .password("MyPassw0rd!")
                .build();

        when(userService.register(any(RegisterCustomerRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    // ---------------------------
    // POST /auth/login - 200
    // ---------------------------
    @Test
    void login_ShouldReturn200_WhenCredentialsValid() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("jdc1@web.co");
        request.setPassword("myPassw0rd!");

        LoginResponse response = new LoginResponse("fake-jwt-token", "Bearer", 3600);
        response.setAccessToken("fake-jwt-token");

        when(authService.login("jdc1@web.co", "myPassw0rd!"))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("fake-jwt-token"));
    }
}
