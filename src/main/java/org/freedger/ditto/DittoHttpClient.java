package org.freedger.ditto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Client for interacting with Ditto's HTTP API.
 */
public class DittoHttpClient {
    private static final int REQUEST_TIMEOUT_SECONDS = 10;
    
    private final String baseUrl;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
    
    /**
     * Creates a new DittoHttpClient with the specified base URL and API key.
     * @param baseUrl The base URL of the Ditto API (e.g., "https://your-app-id.cloud.ditto.live/api/v4/")
     * @param apiKey The API key for authentication
     */
    public DittoHttpClient(String baseUrl, String apiKey) {
        // Ensure the base URL ends with a slash for proper path concatenation
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.objectMapper = new ObjectMapper();
        
        // Create a reusable HttpClient with connection pooling and timeouts
        RequestConfig config = RequestConfig.custom()
            .setConnectionRequestTimeout(Timeout.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .setResponseTimeout(Timeout.ofSeconds(REQUEST_TIMEOUT_SECONDS))
            .build();
            
        this.httpClient = HttpClients.custom()
            .setDefaultRequestConfig(config)
            .setDefaultHeaders(Arrays.asList(new Header[] {
                new BasicHeader("Authorization", "Bearer " + apiKey)
            }))
            .build();
    }

    /**
     * Find ledgers where the user has read or write access.
     * @param userId The user ID to check access for
     * @return List of ledgers the user can access
     */
    public List<DittoLedger> findAccessibleLedgers(String userId) throws IOException {
        try {
            // Build the DQL query with parameters
            String query = String.format(
                "SELECT * FROM %s WHERE array_contains(readerIds, :userId) OR array_contains(writerIds, :userId)",
                DittoLedger.COLLECTION
            );
            
            // Create JSON request body
            ExecuteRequest requestBody = new ExecuteRequest(query, Map.of("userId", userId));
            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            
            // Create and execute the request
            HttpPost httpPost = new HttpPost(baseUrl + "store/execute");
            httpPost.setEntity(new StringEntity(jsonRequestBody, ContentType.APPLICATION_JSON));
            
            // Execute the request
            String responseBody = httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                } else {
                    throw new IOException("Request failed with status code " + statusCode);
                }
            });
            
            // Parse and return response
            DittoQueryResponse<DittoLedger> queryResponse = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructParametricType(DittoQueryResponse.class, DittoLedger.class)
            );
            
            return queryResponse.getItems();
            
        } catch (Exception e) {
            throw new IOException("Failed to query Ditto API: " + e.getMessage(), e);
        }
    }
}
