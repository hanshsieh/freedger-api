package org.freedger;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.microsoft.azure.functions.*;

import org.freedger.dto.ErrorResponse;
import org.freedger.dto.TokenExchangeRequest;
import org.freedger.dto.TokenExchangeResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
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

@ExtendWith(MockitoExtension.class)
class AuthServerTest {
    private static RSAPrivateKey authProviderPrivateKey;
    private static RSAPublicKey authProviderPublicKey;
    private static final String USER_ID = "test-user-123";
    private static final String AUTH_PROVIDER_AUDIENCE = "test-audience";
    private static final String AUTH_PROVIDER_ISSUER = "https://test-issuer.com/";
    private static final String TOKEN_ISSUER = "https://test-issuer.com/";
    private static final String TOKEN_SECRET = "test-token-secret";
    private static final String KEY_ID = "test-key-id";

    @Mock
    private ExecutionContext context;

    @Mock
    private Logger logger;

    @Mock
    private Jwk jwk;

    @Mock
    private JwkProvider jwkProvider;

    @Spy
    private JWTCreator.Builder jwtBuilder;

    @Mock
    private HttpRequestMessage<TokenExchangeRequest> tokenExchangeRequestMsg;

    @Mock
    private TokenExchangeRequest tokenExchangeRequest;

    @Mock
    private HttpResponseMessage response;

    private Map<String, String> requestHeaders;

    private AuthServer authServer;
    
    @BeforeAll
    static void beforeAll() throws Exception {
        // Generate RSA key pair for testing
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        authProviderPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        authProviderPublicKey = (RSAPublicKey) keyPair.getPublic();
    }

    @BeforeEach
    void beforeEach() throws Exception {
        // Set up default request headers
        requestHeaders = new HashMap<>();
        requestHeaders.put("Content-Type", "application/json");

        lenient().when(jwkProvider.get(anyString())).thenReturn(jwk);
        lenient().when(jwk.getPublicKey()).thenReturn(authProviderPublicKey);

        // Mock response builder
        lenient().when(tokenExchangeRequestMsg.createResponseBuilder(any()))
            .thenAnswer(invocation -> new HttpResponseMessageMock.Builder().status(invocation.getArgument(0)));
        
        tokenExchangeRequest = new TokenExchangeRequest();
        lenient().when(tokenExchangeRequestMsg.getBody()).thenReturn(tokenExchangeRequest);

        lenient().when(context.getLogger()).thenReturn(logger);

        authServer = new AuthServer(
            AUTH_PROVIDER_ISSUER,
            AUTH_PROVIDER_AUDIENCE,
            jwkProvider,
            TOKEN_ISSUER,
            TOKEN_SECRET
        );
    }

    @Test
    @DisplayName("Should successfully create AuthServer instance when using default constructor")
    void testConstructor_Default_Success() throws Exception {
        try (MockedStatic<Env> mockedEnv = Mockito.mockStatic(Env.class)) {
            mockedEnv.when(Env::authProviderIssuer).thenReturn(AUTH_PROVIDER_ISSUER);
            mockedEnv.when(Env::authProviderAudience).thenReturn(AUTH_PROVIDER_AUDIENCE);
            mockedEnv.when(Env::authProviderJwks).thenReturn("https://test-issuer.com/.well-known/jwks.json");
            mockedEnv.when(Env::tokenIssuer).thenReturn(TOKEN_ISSUER);
            mockedEnv.when(Env::tokenSecret).thenReturn(TOKEN_SECRET);

            AuthServer authServer = new AuthServer();
            assertNotNull(authServer);
        }
    }

    @Test
    @DisplayName("Should successfully generate exchange token when valid auth token is provided")
    void testCreateDittoExchangeToken_ValidToken_Success() throws Exception {
        // Prepare a valid Auth0 token
        String authToken = createAuthProviderToken();
        tokenExchangeRequest.setToken(authToken);
        
        HttpResponseMessage result = authServer.createDittoExchangeToken(tokenExchangeRequestMsg, context);

        // Verify the response
        assertNotNull(result);
        assertEquals(HttpStatus.OK, result.getStatus());
        
        // Verify the response body contains a token
        TokenExchangeResponse responseBody = (TokenExchangeResponse) result.getBody();
        String exchangeToken = responseBody.getToken();
        assertTrue(exchangeToken != null);

        // Verify the exchange token is valid
        validateExchangeToken(exchangeToken);

        // Verify that the current key ID was used
        verify(jwkProvider, times(1)).get(KEY_ID);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request body is null")
    void testCreateDittoExchangeToken_EmptyRequestBody_BadRequest() {
        // Set up request with null body
        when(tokenExchangeRequestMsg.getBody()).thenReturn(null);
        
        // Execute the test
        HttpResponseMessage response = authServer.createDittoExchangeToken(tokenExchangeRequestMsg, context);
        
        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());

        // Verify the response body contains an error message
        ErrorResponse responseBody = (ErrorResponse) response.getBody();
        assertEquals("Token is required", responseBody.getMessage());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when token is an empty string")
    void testCreateDittoExchangeToken_EmptyToken_Unauthorized() {
        // Create request with empty token
        tokenExchangeRequest.setToken("");
        
        // Execute the test
        HttpResponseMessage response = authServer.createDittoExchangeToken(tokenExchangeRequestMsg, context);
        
        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatus());
        
        // Verify the response body contains an error message
        ErrorResponse responseBody = (ErrorResponse) response.getBody();
        assertTrue(responseBody.getMessage().startsWith("Invalid token"), responseBody.getMessage());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when token format is invalid")
    void testCreateDittoExchangeToken_InvalidToken_Unauthorized() {
        // Create request with invalid token
        tokenExchangeRequest.setToken("invalid.token.here");
        
        // Execute the test
        HttpResponseMessage response = authServer.createDittoExchangeToken(tokenExchangeRequestMsg, context);
        
        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatus());
        ErrorResponse responseBody = (ErrorResponse) response.getBody();
        assertTrue(responseBody.getMessage().startsWith("Invalid token"), responseBody.getMessage());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when token is expired")
    void testCreateDittoExchangeToken_ExpiredToken_Unauthorized() {
        // Create request with expired token
        Date now = new Date();
        Date issuedAt = new Date(now.getTime() - 60 * 60 * 1000);
        Date expiresAt = new Date(now.getTime() - 3 * 60 * 1000);
        
        // Use the private key for signing the token
        String expiredToken = JWT.create()
                .withIssuer(AUTH_PROVIDER_ISSUER)
                .withSubject(USER_ID)
                .withAudience(AUTH_PROVIDER_AUDIENCE)
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withKeyId(KEY_ID)
                .sign(Algorithm.RSA256(null, authProviderPrivateKey));

        tokenExchangeRequest.setToken(expiredToken);
        
        // Execute the test
        HttpResponseMessage response = authServer.createDittoExchangeToken(tokenExchangeRequestMsg, context);
        
        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatus());
        ErrorResponse responseBody = (ErrorResponse) response.getBody();
        assertTrue(responseBody.getMessage().contains("The Token has expired"), responseBody.getMessage());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when token has invalid audience")
    void testCreateDittoExchangeToken_InvalidAudience_Unauthorized() {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + 60 * 60 * 1000);
        
        String token = JWT.create()
                .withIssuer(AUTH_PROVIDER_ISSUER)
                .withSubject(USER_ID)
                .withAudience("invalid-audience")
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .withKeyId(KEY_ID)
                .sign(Algorithm.RSA256(null, authProviderPrivateKey));

        tokenExchangeRequest.setToken(token);
        
        // Execute the test
        HttpResponseMessage response = authServer.createDittoExchangeToken(tokenExchangeRequestMsg, context);
        
        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatus());
        ErrorResponse responseBody = (ErrorResponse) response.getBody();
        assertTrue(responseBody.getMessage().contains("The Claim 'aud' value doesn't contain the required audience"),
            responseBody.getMessage());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when token has invalid issuer")
    void testCreateDittoExchangeToken_InvalidIssuer_Unauthorized() {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + 60 * 60 * 1000);
        
        String token = JWT.create()
                .withIssuer("invalid-issuer")
                .withSubject(USER_ID)
                .withAudience(AUTH_PROVIDER_AUDIENCE)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .withKeyId(KEY_ID)
                .sign(Algorithm.RSA256(null, authProviderPrivateKey));

        tokenExchangeRequest.setToken(token);
        
        // Execute the test
        HttpResponseMessage response = authServer.createDittoExchangeToken(tokenExchangeRequestMsg, context);
        
        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatus());
        ErrorResponse responseBody = (ErrorResponse) response.getBody();
        assertTrue(responseBody.getMessage().contains("The Claim 'iss' value doesn't match the required issuer"),
            responseBody.getMessage());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when token has invalid signature")
    void testCreateDittoExchangeToken_InvalidSignature_Unauthorized() throws Exception {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + 60 * 60 * 1000);

        // Generate another private key to sign the token
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        var privateKey = (RSAPrivateKey) keyPair.getPrivate();
        
        String token = JWT.create()
                .withIssuer(AUTH_PROVIDER_ISSUER)
                .withSubject(USER_ID)
                .withAudience(AUTH_PROVIDER_AUDIENCE)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .withKeyId(KEY_ID)
                .sign(Algorithm.RSA256(null, privateKey));

        tokenExchangeRequest.setToken(token);
        
        // Execute the test
        HttpResponseMessage response = authServer.createDittoExchangeToken(tokenExchangeRequestMsg, context);
        
        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatus());
        ErrorResponse responseBody = (ErrorResponse) response.getBody();
        assertTrue(responseBody.getMessage().contains("The Token's Signature resulted invalid"),
            responseBody.getMessage());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when failed to fetch JWK")
    void testCreateDittoExchangeToken_FailedGetJWK_Unauthorized() throws Exception {
        // Prepare a valid Auth0 token
        String authToken = createAuthProviderToken();
        tokenExchangeRequest.setToken(authToken);

        // Mock failed getJWK
        when(jwkProvider.get(anyString())).thenThrow(new JwkException("Failed to get JWK"));

        // Execute the test
        HttpResponseMessage response = authServer.createDittoExchangeToken(tokenExchangeRequestMsg, context);
        
        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatus());
        ErrorResponse responseBody = (ErrorResponse) response.getBody();
        assertTrue(responseBody.getMessage().contains("Failed to get JWK"),
            responseBody.getMessage());
    }

    @Test
    @DisplayName("Should return 500 Internal Server Error when unknown error occurs while fetching JWK")
    void testCreateDittoExchangeToken_GetJWKUnknownError_Unauthorized() throws Exception {
        // Prepare a valid Auth0 token
        String authToken = createAuthProviderToken();
        tokenExchangeRequest.setToken(authToken);

        // Mock failed getJWK
        when(jwkProvider.get(anyString())).thenThrow(new RuntimeException("Unknown error occurred"));

        // Execute the test
        HttpResponseMessage response = authServer.createDittoExchangeToken(tokenExchangeRequestMsg, context);
        
        // Verify the response
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        ErrorResponse responseBody = (ErrorResponse) response.getBody();
        assertEquals("Internal server error", responseBody.getMessage());
        verify(logger, atLeast(1)).severe(anyString());
    }

    /**
     * Creates a test JWT token for testing purposes
     * @return A signed JWT token string
     */
    private String createAuthProviderToken() {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + 3600 * 1000);
        
        // Use the private key for signing the token
        return JWT.create()
                .withIssuer(AUTH_PROVIDER_ISSUER)
                .withSubject(USER_ID)
                .withAudience(AUTH_PROVIDER_AUDIENCE)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .withKeyId(KEY_ID)
                .sign(Algorithm.RSA256(null, authProviderPrivateKey));
    }

    private void validateExchangeToken(String exchangeToken) {
        // Get the key from the JWKS endpoint
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        
        // Verify the token
        JWTVerifier verifier = JWT.require(algorithm)
            .withIssuer(TOKEN_ISSUER)
            .withAudience(TOKEN_ISSUER)
            .withSubject(USER_ID)
            .build();
            
        DecodedJWT jwt = verifier.verify(exchangeToken);
        Date expiresAt = jwt.getExpiresAt();
        Date now = new Date();
        Date maxExpiresAt = new Date(now.getTime() + 3600 * 1000);
        Date minExpiresAt = new Date(maxExpiresAt.getTime() - 10 * 1000);
        assertTrue(expiresAt.before(maxExpiresAt));
        assertTrue(expiresAt.after(minExpiresAt));
    }
}
