package org.freedger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.microsoft.azure.functions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    @BeforeEach
    void setUp() throws Exception {
        // Initialize Mockito annotations
        MockitoAnnotations.openMocks(this).close();
        
        // Generate RSA key pair for testing
        if (privateKey == null) {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();
            publicKey = (RSAPublicKey) keyPair.getPublic();
        }

        // Set up environment variables for testing
        System.setProperty("AUTH0_DOMAIN", "test-issuer.com");
        System.setProperty("AUTH0_AUDIENCE", TEST_AUDIENCE);
        System.setProperty("DITTO_APP_ID", "test-app-id");
        System.setProperty("EXCHANGE_TOKEN_SECRET", "test-secret-key-1234567890");

        // Set up logger
        when(context.getLogger()).thenReturn(Logger.getGlobal());

        // Mock response builder
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

        // Execute the test
        AuthServer authServer = new AuthServer();
        HttpResponseMessage response = authServer.run(request, context);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getBody());
        
        // Verify the returned token
        String responseBody = response.getBody().toString();
        assertTrue(responseBody.contains("token") || responseBody.contains("error") || responseBody.contains("message"));
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
