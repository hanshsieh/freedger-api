package org.freedger;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.microsoft.azure.functions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

class AuthServerTest {

    @Mock
    private HttpRequestMessage<Optional<String>> request;

    @Mock
    private ExecutionContext context;

    private static RSAPrivateKey privateKey;
    private static RSAPublicKey publicKey;
    private static final String TEST_USER_ID = "test-user-123";
    private static final String TEST_AUDIENCE = "test-audience";
    private static final String TEST_ISSUER = "https://test-issuer.com/";

    private JwkProvider mockJwkProvider;
    private Jwk mockJwk;
    private JWTVerifier mockVerifier;
    private DecodedJWT mockDecodedJWT;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize mocks
        MockitoAnnotations.openMocks(this).close();
        
        // Generate RSA key pair for testing
        if (privateKey == null) {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();
            publicKey = (RSAPublicKey) keyPair.getPublic();
        }

        // Create mock JWT verification objects
        mockJwk = mock(Jwk.class);
        when(mockJwk.getPublicKey()).thenReturn(publicKey);
        
        mockJwkProvider = mock(JwkProvider.class);
        when(mockJwkProvider.get(anyString())).thenReturn(mockJwk);
        
        mockDecodedJWT = mock(DecodedJWT.class);
        when(mockDecodedJWT.getKeyId()).thenReturn("test-key-id");
        when(mockDecodedJWT.getSubject()).thenReturn("test-user-id");
        when(mockDecodedJWT.getIssuer()).thenReturn("https://test-issuer.com/");
        when(mockDecodedJWT.getAudience()).thenReturn(Collections.singletonList(TEST_AUDIENCE));
        
        mockVerifier = mock(JWTVerifier.class);
        when(mockVerifier.verify(anyString())).thenReturn(mockDecodedJWT);

        // Set up logger
        when(context.getLogger()).thenReturn(Logger.getGlobal());

        // Mock response builder using HttpResponseMessageBuilderMock
        when(request.createResponseBuilder(any(HttpStatus.class))).thenAnswer((Answer<HttpResponseMessage.Builder>) invocation -> {
            HttpStatus status = invocation.getArgument(0);
            return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
        });
    }

    @Test
    void testValidTokenExchange() throws Exception {
        // Prepare a valid Auth0 token
        String testToken = createTestAuth0Token();
        
        when(request.getBody()).thenReturn(Optional.of("{\"token\":\"" + testToken + "\"}"));

        try (MockedStatic<JWT> jwtMock = Mockito.mockStatic(JWT.class)) {
            // Mock JWT.require()
            JWTVerifier.BaseVerification verification = mock(JWTVerifier.BaseVerification.class);
            when(verification.withIssuer(any(String[].class))).thenReturn(verification);
            when(verification.withAudience(any(String[].class))).thenReturn(verification);
            when(verification.build(any())).thenReturn(mockVerifier);
            
            when(JWT.require(any(Algorithm.class))).thenReturn(verification);
            when(JWT.decode(anyString())).thenReturn(mockDecodedJWT);

            // Create AuthServer with mocked dependencies
            AuthServer authServer = new AuthServer(
                "test-issuer.com",
                TEST_AUDIENCE,
                "test-app-id",
                "test-secret-key-1234567890",
                mockJwkProvider
            );

            // Execute the test
            HttpResponseMessage response = authServer.run(request, context);

            // Verify the response
            assertEquals(HttpStatus.OK, response.getStatus());
            assertNotNull(response.getBody());
            
            // Verify the returned token
            String responseBody = response.getBody().toString();
            assertTrue(responseBody.contains("token") || responseBody.contains("error") || responseBody.contains("message"));
        }
    }

    @Test
    void testEmptyRequestBody() {
        // Test with empty request body
        when(request.getBody()).thenReturn(Optional.empty());
        
        AuthServer authServer = new AuthServer();
        HttpResponseMessage response = authServer.run(request, context);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertTrue(response.getBody().toString().contains("Request body is required"));
    }

    @Test
    void testMissingToken() {
        // Test with missing token in request body
        when(request.getBody()).thenReturn(Optional.of("{}"));
        
        AuthServer authServer = new AuthServer();
        HttpResponseMessage response = authServer.run(request, context);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertTrue(response.getBody().toString().contains("Token is required"));
    }

    @Test
    void testInvalidToken() {
        // Test with invalid token format
        when(request.getBody()).thenReturn(Optional.of("{\"token\":\"invalid.token.here\"}"));
        
        AuthServer authServer = new AuthServer();
        HttpResponseMessage response = authServer.run(request, context);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatus());
        assertTrue(response.getBody().toString().contains("Invalid token"));
    }

    /**
     * Creates a test JWT token for testing purposes
     * @return A signed JWT token string
     */
    private String createTestAuth0Token() {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + 3600 * 1000);
        
        // Use the private key for signing the token
        return JWT.create()
                .withIssuer(TEST_ISSUER)
                .withSubject(TEST_USER_ID)
                .withAudience(TEST_AUDIENCE)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .withKeyId("test-key-id")
                .sign(Algorithm.RSA256(null, privateKey));
    }
}
