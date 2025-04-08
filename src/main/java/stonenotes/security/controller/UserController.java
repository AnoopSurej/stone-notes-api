package stonenotes.security.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stonenotes.common.ApiResponse;
import stonenotes.dto.UserLoginDto;
import stonenotes.dto.UserRegistrationDto;
import stonenotes.exception.EmailAlreadyExistsException;
import stonenotes.security.jwt.JwtTokenProvider;
import stonenotes.service.UserService;

import java.util.Collections;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        try {
            userService.registerUser(userRegistrationDto);
            ApiResponse<String> response = ApiResponse.success("User registered successfully", "Registration successful");
            return ResponseEntity.ok(response);
        } catch (EmailAlreadyExistsException e) {
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody UserLoginDto userLoginDto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDto.getEmail(), userLoginDto.getPassword()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(userLoginDto.getEmail());
        String token = jwtTokenProvider.generateToken(userDetails);

        ApiResponse<String> response = ApiResponse.success(token, "Login successful", 200);
        return ResponseEntity.ok(response);
    }
}
