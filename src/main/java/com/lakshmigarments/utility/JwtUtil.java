package com.lakshmigarments.utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.security.MessageDigest;

/**
 * Utility class for handling JWT operations and password hashing.
 * <p>
 * Provides methods to generate JWT tokens, extract claims and username from tokens,
 * validate tokens, and hash passwords using SHA-256.
 * </p>
 */
@Component
public class JwtUtil {

    /**
     * Secret key used for signing the JWT tokens.
     * Should be kept secure and strong to ensure token integrity.
     */
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * JWT token expiration time in milliseconds.
     * Current setting: 1 hour = 3600000 ms.
     */
    private final long jwtExpirationInMs = 3600000;

    /**
     * Generates a JWT token using the provided username and claims.
     *
     * @param username the subject or username for whom the token is generated.
     * @param claims   additional claims to be included in the JWT payload.
     * @return a signed JWT token as a compact string.
     */
    public String generateToken(String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token JWT token string.
     * @return the username contained in the token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from the JWT token using the claim resolver function.
     *
     * @param token         JWT token string.
     * @param claimResolver a function to extract a particular claim from the Claims object.
     * @param <T>           the type of the claim to extract.
     * @return extracted claim value.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claimResolver.apply(claims);
    }

    /**
     * Validates the JWT token by checking username match and token expiration.
     *
     * @param token    JWT token string.
     * @param username expected username to match with token subject.
     * @return true if token is valid and not expired; false otherwise.
     */
    public boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * Checks if the JWT token is expired based on its expiration claim.
     *
     * @param token JWT token string.
     * @return true if token is expired, false otherwise.
     */
    private boolean isTokenExpired(String token) {
        final Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    /**
     * Hashes a plaintext password using SHA-256 algorithm.
     *
     * @param password the plaintext password.
     * @return the hexadecimal string representation of the SHA-256 hash.
     * @throws RuntimeException if SHA-256 algorithm is not available.
     */
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param bytes array of bytes.
     * @return hexadecimal string representation.
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
