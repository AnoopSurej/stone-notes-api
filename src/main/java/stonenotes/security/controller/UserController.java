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
import stonenotes.dto.LoginResponseDto;
import stonenotes.dto.RefreshTokenRequestDto;
import stonenotes.dto.UserLoginDto;
import stonenotes.dto.UserRegistrationDto;
import stonenotes.exception.EmailAlreadyExistsException;
import stonenotes.model.RefreshToken;
import stonenotes.model.User;
import stonenotes.security.jwt.JwtTokenProvider;
import stonenotes.service.RefreshTokenService;
import stonenotes.service.UserService;

import java.util.Collections;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
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
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody UserLoginDto userLoginDto) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDto.getEmail(), userLoginDto.getPassword()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(userLoginDto.getEmail());
        String accessToken = jwtTokenProvider.generateToken(userDetails);

        Long userId = userService.getUserIdByEmail(userLoginDto.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userId);

        LoginResponseDto loginResponse = new LoginResponseDto(accessToken, refreshToken.getToken());
        ApiResponse<LoginResponseDto> response = ApiResponse.success(loginResponse, "Login successful", 200);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponseDto>> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        refreshToken = refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateToken(user);

        LoginResponseDto loginResponse = new LoginResponseDto(newAccessToken, refreshToken.getToken());
        ApiResponse<LoginResponseDto> response = ApiResponse.success(loginResponse, "Token refreshed", 200);
        return ResponseEntity.ok(response);
    }
}
