package org.freedger;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.microsoft.azure.functions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DittoApiTest {
    private static RSAPrivateKey authProviderPrivateKey;
    private static RSAPublicKey authProviderPublicKey;
    private static final String SUBJECT = "auth0|685e35fe029584349202c39d";
    private static final String[] AUTH_PROVIDER_AUDIENCES = {"https://api.freedger.org/", "https://freedger-dev.jp.auth0.com/userinfo"};
    private static final String AUTH_PROVIDER_ISSUER = "https://freedger-dev.jp.auth0.com/";
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
    private HttpResponseMessage response;

    private Map<String, String> requestHeaders;

    private DittoApi authServer;
    
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
        lenient().when(context.getLogger()).thenReturn(logger);
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
                .withSubject(SUBJECT)
                .withAudience(AUTH_PROVIDER_AUDIENCES)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .withKeyId(KEY_ID)
                .sign(Algorithm.RSA256(null, authProviderPrivateKey));
    }
}
