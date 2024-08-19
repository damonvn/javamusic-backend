package com.example.javamusic.util;
import com.example.javamusic.model.AccessTokenUserInfo;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.Key;


@Component
public class JwtUtils {
    
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${app.jwtRefreshExpirationMs}")
    private int jwtRefreshExpirationMs;

    private Key key;

    public JwtUtils() {
    }

    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        this.key = new SecretKeySpec(keyBytes, SignatureAlgorithm.HS512.getJcaName());
    }

    public String generateAccessToken(String name, String email, String role) {
        try {
            return Jwts.builder()
                .setSubject(email)
                .claim("name", name)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
        } catch (Exception e) {
            System.out.println("Error generating token: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public AccessTokenUserInfo getUserInfoFromAccessToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        AccessTokenUserInfo userInfo = new AccessTokenUserInfo();
        userInfo.setName(claims.get("name", String.class));
        userInfo.setEmail(claims.get("email", String.class));
        userInfo.setRole(claims.get("role", String.class));
        return userInfo;
    }

    public AccessTokenUserInfo getUserInfoFromExpiredAccessToken(String token) {
    try {
        // Trích xuất thông tin từ token, kể cả khi token đã hết hạn
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        AccessTokenUserInfo userInfo = new AccessTokenUserInfo();
        userInfo.setName(claims.get("name", String.class));
        userInfo.setEmail(claims.get("email", String.class));
        userInfo.setRole(claims.get("role", String.class));
        
        return userInfo;
        
    } catch (ExpiredJwtException e) {
        // Khi token đã hết hạn, vẫn có thể lấy thông tin người dùng từ claims trong ngoại lệ
        Claims claims = e.getClaims();
        
        AccessTokenUserInfo userInfo = new AccessTokenUserInfo();
        userInfo.setName(claims.get("name", String.class));
        userInfo.setEmail(claims.get("email", String.class));
        userInfo.setRole(claims.get("role", String.class));
        
        return userInfo;
    }
}

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public void setRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(jwtRefreshExpirationMs / 1000);
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.addCookie(cookie);
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
