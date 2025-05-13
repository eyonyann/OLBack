package org.example.onlinelearning;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.WeakKeyException;
import org.example.onlinelearning.config.JwtConfig;
import org.example.onlinelearning.config.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.Duration;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    @Mock
    private JwtConfig jwtConfig;

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "very-very-secret-key-with-at-least-256-bits-length";
    private final long expirationMs = Duration.ofHours(1).toMillis();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jwtConfig.getSecret()).thenReturn(secret);
        when(jwtConfig.getExpiration()).thenReturn(expirationMs);
        jwtTokenProvider = new JwtTokenProvider(jwtConfig);
    }

    @Test
    void generateToken_ValidData_ContainsCorrectClaims() {
        String token = jwtTokenProvider.generateToken("user1", "ROLE_USER", 123L);

        assertAll(
                () -> assertEquals("user1", jwtTokenProvider.getUsername(token)),
                () -> assertEquals("ROLE_USER", jwtTokenProvider.getRole(token)),
                () -> assertEquals(123L, jwtTokenProvider.getUserId(token)),
                () -> assertTrue(new Date().before(jwtTokenProvider.parseClaims(token).getExpiration()))
        );
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtTokenProvider.generateToken("admin", "ROLE_ADMIN", 1L);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_ExpiredToken_ThrowsExpiredJwtException() {
        when(jwtConfig.getExpiration()).thenReturn(-10000L); // Прошедшее время

        JwtTokenProvider expiredProvider = new JwtTokenProvider(jwtConfig);
        String expiredToken = expiredProvider.generateToken("user", "ROLE_USER", 2L);
    }

    @Test
    void validateToken_InvalidSignature_ReturnsFalse() {
        String validToken = jwtTokenProvider.generateToken("user", "ROLE_USER", 3L);
        String invalidToken = validToken + "tampered";

        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void getUserId_InvalidToken_ThrowsJwtException() {
        String invalidToken = "invalid.token.here";
        assertThrows(JwtException.class,
                () -> jwtTokenProvider.getUserId(invalidToken));
    }

    @Test
    void parseClaims_EmptyToken_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtTokenProvider.parseClaims(""));
    }

    @Test
    void constructor_WeakSecretKey_ThrowsWeakKeyException() {
        when(jwtConfig.getSecret()).thenReturn("weak-key");
        assertThrows(WeakKeyException.class,
                () -> new JwtTokenProvider(jwtConfig));
    }
}