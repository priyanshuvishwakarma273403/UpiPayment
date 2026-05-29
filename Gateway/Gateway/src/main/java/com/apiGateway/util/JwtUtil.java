package com.apiGateway.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * ================================================================
 * JWT Utility - API Gateway Version
 * ================================================================
 * API Gateway mein sirf TOKEN VALIDATE karna hai, generate nahi.
 * Generate karna auth-service ka kaam hai.
 *
 * Yeh class WebFlux (reactive) environment mein kaam karti hai.
 * Blocking calls nahi hain - sirf in-memory JWT operations.
 *
 * Important: Yeh same secret key use karta hai jo auth-service use karti hai.
 * Dono services mein jwt.secret same hona chahiye.
 * ================================================================
 */

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Token valid hai ya nahi check karo
     * Signature, expiry sab check hota hai
     */
    public boolean isTokenValid(String token) {
        try{
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        }catch (ExpiredJwtException e) {
            log.warn("JWT token expired");
            return false;
        } catch (MalformedJwtException e) {
            log.warn("JWT token malformed");
            return false;
        } catch (SignatureException e) {
            log.warn("JWT signature invalid");
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token unsupported");
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty");
            return false;
        }
    }

    /** Token se userId nikalo - downstream header ke liye */
    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userId = claims.get("userId");
        return userId != null ? userId.toString() : "";
    }

    /** Token se email nikalo */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /** Token se roles nikalo */
    public String extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object roles = claims.get("roles");
        return roles != null ? roles.toString() : "";
    }

//    ** Token type nikalo: ACCESS ya REFRESH */
    public String extractTokenType(String token) {
        Claims claims = extractAllClaims(token);
        Object type = claims.get("type");
        return type != null ? type.toString() : "";
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
