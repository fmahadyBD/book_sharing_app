package com.fmahadybd.book_network_api_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    // Secret key for signing JWT tokens, injected from application properties
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // JWT expiration time, also injected from application properties
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token the JWT token
     * @return the username (subject) contained in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a claim from the JWT token using the provided claim resolver.
     *
     * @param token the JWT token
     * @param claimsResolver the function to extract the claim
     * @param <T> the type of the claim
     * @return the claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates a JWT token for a user with default claims.
     *
     * @param userDetails the user details to generate the token for
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates a JWT token for a user with custom claims.
     *
     * @param extraClaims additional claims to include in the token
     * @param userDetails the user details to generate the token for
     * @return the generated JWT token
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Builds the JWT token with custom claims and user details.
     *
     * @param extraClaims additional claims to include in the token
     * @param userDetails the user details to generate the token for
     * @param expiration the expiration time of the token
     * @return the generated JWT token
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        // Extract authorities from the user's granted authorities and convert to a list of strings
        var authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // Build the JWT token using the Jwts.builder()
        return Jwts
                .builder()
                .setClaims(extraClaims)  // Set the additional claims
                .setSubject(userDetails.getUsername())  // Set the subject (username)
                .setIssuedAt(new Date(System.currentTimeMillis()))  // Set the issue time (current time)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))  // Set expiration time
                .claim("authorities", authorities)  // Add authorities as a claim
                .signWith(getSignInKey())  // Sign the token using the signing key
                .compact();  // Generate and return the token
    }

    /**
     * Validates the JWT token by checking the username and expiration.
     *
     * @param token the JWT token
     * @param userDetails the user details for the user
     * @return true if the token is valid, otherwise false
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Checks if the JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, otherwise false
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the JWT token.
     *
     * @param token the JWT token
     * @return the expiration date of the token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts all claims from the JWT token.
     *
     * @param token the JWT token
     * @return the claims contained in the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())  // Set the signing key for token parsing
                .build()
                .parseClaimsJws(token)  // Parse the token to get the claims
                .getBody();  // Return the claims body
    }

    /**
     * Generates the signing key from the secret key.
     *
     * @return the signing key
     */
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);  // Decode the secret key
        return Keys.hmacShaKeyFor(keyBytes);  // Generate and return the signing key
    }
}
