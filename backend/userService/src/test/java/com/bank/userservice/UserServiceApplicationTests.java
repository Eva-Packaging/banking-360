package com.bank.userservice;

import com.bank.userservice.controller.UserController;
import com.bank.userservice.dto.LoginResponse;
import com.bank.userservice.dto.RegisterCustomerRequest;
import com.bank.userservice.dto.RegisterCustomerResponse;
import com.bank.userservice.entity.Role;
import com.bank.userservice.entity.User;
import com.bank.userservice.exception.EmailAlreadyExistsException;
import com.bank.userservice.exception.UnauthorizedResponseException;
import com.bank.userservice.repository.RoleRepository;
import com.bank.userservice.repository.UserRepository;
import com.bank.userservice.security.JwtUtil;
import com.bank.userservice.service.AuthService;
import com.bank.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceApplicationTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtUtil jwt;

	@Mock
	private RoleRepository roleRepository;

	@InjectMocks
	private UserService userService;

	@InjectMocks
	private AuthService authService;

	@InjectMocks
	private UserController userController;

	private User user;
	private RegisterCustomerRequest registerRequest;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setId(UUID.randomUUID());
		user.setFirstName("john");
		user.setLastName("doe");
		user.setEmail("jdc1@web.co");
		user.setPhoneNumber("111-111-1111");
		user.setPasswordHash("encodedPassword");

		registerRequest = new RegisterCustomerRequest();
		registerRequest.setEmail("jdc1@web.co");
		registerRequest.setPassword("myPassw0rd!");
	}

	@Test
	void register_ShouldSaveUser_WhenEmailDoesNotExist() {

		when(userRepository.findByEmail("jdc1@web.co"))
				.thenReturn(Optional.empty());

		Role role = new Role();
		role.setName("CUSTOMER");

		when(roleRepository.findByName("CUSTOMER"))
				.thenReturn(Optional.of(role));

		when(userRepository.saveAndFlush(any(User.class)))
				.thenReturn(user);

		RegisterCustomerResponse result = userService.register(registerRequest);

		assertNotNull(result);
		assertEquals("jdc1@web.co", result.getEmail());

		verify(userRepository).findByEmail("jdc1@web.co");
		verify(userRepository).saveAndFlush(any(User.class));
	}

	@Test
	void login_ShouldReturnUser_WhenCredentialsAreValid() {
		when(userRepository.findByEmail("jdc1@web.co"))
				.thenReturn(Optional.of(user));

		when(passwordEncoder.matches(
				"myPassw0rd!",
				"encodedPassword"))
				.thenReturn(true);

		LoginResponse result = authService.login(
				"jdc1@web.co",
				"myPassw0rd!");

		assertNotNull(result);

		verify(userRepository).findByEmail("jdc1@web.co");
		verify(passwordEncoder)
				.matches("myPassw0rd!", "encodedPassword");
	}

	@Test
	void login_ShouldThrowException_WhenLoginIsInvalid_InvalidEmail() {
		when(userRepository.findByEmail("unknown@web.co"))
				.thenReturn(Optional.empty());

		Exception exception = assertThrows(
				UnauthorizedResponseException.class,
				() -> authService.login("unknown@web.co", "myPassw0rd!")
		);

		assertEquals("Invalid credentials", exception.getMessage());
	}
	@Test
	void login_ShouldThrowException_WhenLoginIsInvalid_InvalidPassword() {
		when(userRepository.findByEmail("jdc1@web.co"))
				.thenReturn(Optional.of(user));

		when(passwordEncoder.matches(
				"wrongPassword",
				"encodedPassword"))
				.thenReturn(false);

		Exception exception = assertThrows(
				UnauthorizedResponseException.class,
				() -> authService.login("jdc1@web.co", "wrongPassword")
		);

		assertEquals("Invalid credentials", exception.getMessage());
	}

	@Test
	void register_ShouldThrowException_WhenEmailAlreadyExists() {
		when(userRepository.findByEmail("jdc1@web.co"))
				.thenReturn(Optional.of(user));

		Exception exception = assertThrows(
				EmailAlreadyExistsException.class,
				() -> userService.register(registerRequest)
		);

		assertEquals(
				"Email already in use: jdc1@web.co",
				exception.getMessage()
		);

		verify(userRepository).findByEmail("jdc1@web.co");
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void getCurrentUser_ShouldReturnProfile_WhenUserExists() {

		String token = "valid-token";
		String authHeader = "Bearer " + token;
		String email = "jdc1@web.co";

		Role role = new Role();
		role.setName("CUSTOMER");

		User user = new User();
		user.setId(UUID.randomUUID());
		user.setFirstName("john");
		user.setLastName("doe");
		user.setEmail(email);
		user.setStatus("ACTIVE");
		user.setCreatedAt(LocalDateTime.now());
		user.setRoles(Set.of(role));

		when(jwt.extractEmail(token))
				.thenReturn(email);

		when(userRepository.findByEmail(email))
				.thenReturn(Optional.of(user));

		ResponseEntity<RegisterCustomerResponse> response =
				userController.getCurrentUser(authHeader);

		assertNotNull(response);
		assertEquals(200, response.getStatusCodeValue());

		RegisterCustomerResponse body = response.getBody();

		assertNotNull(body);
		assertEquals(email, body.getEmail());
		assertEquals("john", body.getFirstName());
		assertEquals("doe", body.getLastName());
		assertTrue(body.getRoles().contains("CUSTOMER"));

		verify(jwt).extractEmail(token);
		verify(userRepository).findByEmail(email);
	}
}
