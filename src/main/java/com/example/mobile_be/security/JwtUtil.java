package com.example.mobile_be.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
  SecretKey key = Keys.hmacShaKeyFor("zemo_super_secret_key_2024_secure_for_jwt_demo".getBytes(StandardCharsets.UTF_8));

 private final long EXPIRATION_TIME = 14 * 24 * 60 * 60 * 1000L; //14 ngay

  // Tao token tu user name
  public String generateToken(UserDetails userDetails) {
    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .claim("roles", userDetails.getAuthorities()
            .stream()
            .map(auth -> auth.getAuthority().replace("ROLE_", ""))
            .toList())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  // parse toke de lay toan bo thong tin ben trong
  public Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  // lay user email tu token
  public String extractUserEmail(String token) {
    return extractAllClaims(token).getSubject();
  }

  // Lay thoi gian het han cua token
  public Date extractExpiration(String token) {
    return extractAllClaims(token).getExpiration();
  }

  // kiem tra token con hieu luc k
  public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  // kiem tra token co hop le k
  public boolean isValidateToken(String token, UserDetails userDetails) {
    final String userName = extractUserEmail(token);
    return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }
}
