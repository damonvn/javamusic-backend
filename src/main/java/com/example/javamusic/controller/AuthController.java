package com.example.javamusic.controller;

import com.example.javamusic.model.User;
import com.example.javamusic.model.LoginModel;
import com.example.javamusic.repository.UserRepository;
import com.example.javamusic.service.RefreshTokenService;
import com.example.javamusic.model.AccessTokenUserInfo;
import com.example.javamusic.util.JwtUtils;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.http.HttpHeaders;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Autowired
    private JwtUtils jwtUtils;
 
    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login (@RequestBody LoginModel login){
        Optional<User> optionalUser = userRepository.findByEmail(login.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getPassword().equals(login.getPassword())) {
                String accessToken = jwtUtils.generateAccessToken(user.getName(), user.getEmail(), user.getRole());
                String refreshToken  = refreshTokenService.createRefreshToken(user.getEmail());
                jwtUtils.setRefreshTokenCookie(refreshToken);

                AccessTokenUserInfo userInfo = jwtUtils.getUserInfoFromAccessToken(accessToken);
                return ResponseEntity.ok(Map.of(
                    "access_token", accessToken,
                    "name", userInfo.getName(),
                    "email", userInfo.getEmail(),
                    "role", userInfo.getRole()
                ));
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password")); 
    }

    // 
    @GetMapping("/account")
    public ResponseEntity<?> authenticateAndRefreshToken() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);
                try {
                    AccessTokenUserInfo userInfo = jwtUtils.getUserInfoFromAccessToken(accessToken);
                    return ResponseEntity.ok(Map.of(
                            "message", "Token is valid",
                            "access_token", accessToken,
                            "user", userInfo.getName(),
                            "email", userInfo.getEmail(),
                            "role", userInfo.getRole()));
                } catch (ExpiredJwtException e) {
                    String refreshToken = jwtUtils.getRefreshTokenFromCookie(request);
                    if (refreshToken != null) {
                        Optional<String> validatedToken = refreshTokenService.validateRefreshToken(refreshToken);                      
                        if (validatedToken.isPresent()) {
                            var userInfo = jwtUtils.getUserInfoFromExpiredAccessToken(accessToken);
                            String newAccessToken = jwtUtils.generateAccessToken(userInfo.getName(), userInfo.getEmail(), userInfo.getRole());
                            String newRefreshToken = refreshTokenService.createRefreshToken(userInfo.getEmail());

                            jwtUtils.setRefreshTokenCookie(newRefreshToken);

                            return ResponseEntity.ok(Map.of(
                                    "access_token", newAccessToken,
                                    "name", userInfo.getName(),
                                    "email", userInfo.getEmail(),
                                    "role", userInfo.getRole(),
                                    "message", "Refresh token is valid"));
                        }
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Refresh token is invalid or expired"));
                    }
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Refresh token is null"));
                } catch (Exception e) {
                    e.printStackTrace(); // Log exception for debugging
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("message", "Access token is invalid or an unexpected error occurred"));
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Access token is null or invalid"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("No valid HTTP request context available");
    }
}
