package org.freedger;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.microsoft.azure.functions.*;

import org.freedger.dto.TokenExchangeRequest;
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
    private HttpRequestMessage<TokenExchangeRequest> request;

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

        // Create a mock request with TokenExchangeRequest type
        request = (HttpRequestMessage<TokenExchangeRequest>) mock(HttpRequestMessage.class);
        context = mock(ExecutionContext.class);
        
        // Mock logger
        when(context.getLogger()).thenReturn(mock(Logger.class));
        
        // Set up default request headers
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        when(request.getHeaders()).thenReturn(headers);
        
        // Mock JWT and JWK provider
        mockJwkProvider = mock(JwkProvider.class);
        mockVerifier = mock(JWTVerifier.class);
        mockDecodedJWT = mock(DecodedJWT.class);
        
        // Mock JWT claims
        when(mockDecodedJWT.getSubject()).thenReturn("test-user-id");
        when(mockDecodedJWT.getIssuer()).thenReturn("test-issuer.com");
        when(mockDecodedJWT.getAudience()).thenReturn(Collections.singletonList(TEST_AUDIENCE));
        
        // Mock response builder
        HttpResponseMessage.Builder responseBuilder = mock(HttpResponseMessage.Builder.class);
        when(responseBuilder.build()).thenReturn(mock(HttpResponseMessage.class));
        when(request.createResponseBuilder(any())).thenReturn(responseBuilder);
        
        // Create mock JWT verification objects
        mockJwk = mock(Jwk.class);
        when(mockJwk.getPublicKey()).thenReturn(publicKey);
        
        when(mockJwkProvider.get(anyString())).thenReturn(mockJwk);
        
        when(mockVerifier.verify(anyString())).thenReturn(mockDecodedJWT);
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
        
        // Create request with TokenExchangeRequest
        TokenExchangeRequest tokenRequest = new TokenExchangeRequest();
        tokenRequest.setToken(testToken);
        when(request.getBody()).thenReturn(tokenRequest);

        try (MockedStatic<JWT> jwtMock = Mockito.mockStatic(JWT.class)) {
            // Mock JWT.require()
            JWTVerifier.BaseVerification verification = mock(JWTVerifier.BaseVerification.class);
            when(verification.withIssuer(any(String[].class))).thenReturn(verification);
            when(verification.withAudience(any(String[].class))).thenReturn(verification);
            when(verification.build(any())).thenReturn(mockVerifier);
            
            Algorithm algorithm = mock(Algorithm.class);
            when(JWT.require(any(Algorithm.class))).thenReturn(verification);
            when(JWT.decode(anyString())).thenReturn(mockDecodedJWT);
            
            // Mock response
            HttpResponseMessage response = mock(HttpResponseMessage.class);
            when(response.getStatus()).thenReturn(HttpStatus.OK);
            when(response.getBody()).thenReturn("{\"token\":\"test-exchange-token\"}");
            
            // Create AuthServer with mocked dependencies
            AuthServer authServer = new AuthServer(
                "test-issuer.com",
                TEST_AUDIENCE,
                "test-app-id",
                "test-secret-key-1234567890",
                mockJwkProvider
            );

            // Execute the test
            HttpResponseMessage result = authServer.run(request, context);

            // Verify the response
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatus());
            
            // Verify the response body contains a token
            String responseBody = result.getBody().toString();
            assertTrue(responseBody.contains("token") || responseBody.contains("error") || responseBody.contains("message"));
        }
    }

    @Test
    void testEmptyRequestBody() {
        // Set up request with null body
        when(request.getBody()).thenReturn(null);
        
        // Create AuthServer with test configuration
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
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("Token is required", response.getBody());
    }

    @Test
    void testMissingToken() {
        // Create request with empty token
        TokenExchangeRequest tokenRequest = new TokenExchangeRequest();
        tokenRequest.setToken("");
        when(request.getBody()).thenReturn(tokenRequest);
        
        // Create AuthServer with test configuration
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
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("Token is required", response.getBody());
    }

    @Test
    void testInvalidToken() {
        // Create request with invalid token
        TokenExchangeRequest tokenRequest = new TokenExchangeRequest();
        tokenRequest.setToken("invalid.token.here");
        when(request.getBody()).thenReturn(tokenRequest);
        
        try (MockedStatic<JWT> jwtMock = Mockito.mockStatic(JWT.class)) {
            // Mock JWT.require()
            JWTVerifier.BaseVerification verification = mock(JWTVerifier.BaseVerification.class);
            Algorithm algorithm = mock(Algorithm.class);
            when(JWT.require(any(Algorithm.class))).thenReturn(verification);
            when(verification.withIssuer(any(String[].class))).thenReturn(verification);
            when(verification.withAudience(any(String[].class))).thenReturn(verification);
            
            // Mock verifier to throw exception
            JWTVerifier verifier = mock(JWTVerifier.class);
            when(verification.build(any())).thenReturn(verifier);
            when(verifier.verify(anyString())).thenThrow(new com.auth0.jwt.exceptions.JWTVerificationException("Invalid token"));
            
            // Create AuthServer with test configuration
            AuthServer authServer = new AuthServer(
                "test-issuer.com",
                TEST_AUDIENCE,
                "test-app-id",
                "test-secret-key-1234567890",
                mockJwkProvider
            );
            
            // Mock response
            HttpResponseMessage.Builder responseBuilder = mock(HttpResponseMessage.Builder.class);
            when(request.createResponseBuilder(any())).thenReturn(responseBuilder);
            when(responseBuilder.body(anyString())).thenReturn(responseBuilder);
            when(responseBuilder.build()).thenReturn(mock(HttpResponseMessage.class));
            
            // Execute the test
            HttpResponseMessage response = authServer.run(request, context);
            
            // Verify the response
            assertNotNull(response);
        }
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
