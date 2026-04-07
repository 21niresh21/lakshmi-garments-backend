package com.lakshmigarments.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Value("${lakshmigarments.app.jwtSecret}")
	private String jwtSecret;

	@Value("${lakshmigarments.app.jwtExpirationMs}")
	private int jwtExpirationMs;

	// 1. Generate Token
	public String generateJwtToken(Authentication authentication) {
		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

		return Jwts.builder().subject(userPrincipal.getUsername()).issuedAt(new Date())
				.expiration(new Date((new Date()).getTime() + jwtExpirationMs)).signWith(getSigningKey()).compact();
	}

	// 2. Helper to get the Signing Key (Modern JJWT 0.12.x way)
	private SecretKey getSigningKey() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	// 3. Extract Username
	public String getUserNameFromJwtToken(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	// 4. Extract Expiration Date (The method you were missing!)
	public Date getExpirationDateFromToken(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	// 5. Generic method to extract any claim
	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	// 6. Validate Token
	public boolean validateJwtToken(String authToken) {
		try {
			Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
			return true;
		} catch (JwtException e) {
			logger.error("Invalid JWT: {}", e.getMessage());
		}
		return false;
	}
}