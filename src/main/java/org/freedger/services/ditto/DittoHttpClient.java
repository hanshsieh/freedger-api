package org.freedger.services.ditto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.util.Timeout;
import org.freedger.services.ditto.models.Account;
import org.freedger.services.ditto.models.AccountConfig;
import org.freedger.services.ditto.models.Ledger;
import org.freedger.services.ditto.models.LedgerConfig;
import org.freedger.services.ditto.models.QueryRequest;
import org.freedger.services.ditto.models.QueryResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client for interacting with Ditto's HTTP API.
 */
public class DittoHttpClient {
    private static final int REQUEST_TIMEOUT_SECONDS = 10;
    
    private final String baseUrl;
    private final Gson gson;
    private final CloseableHttpClient httpClient;
    
    /**
     * Creates a new DittoHttpClient with the specified base URL and API key.
     * @param baseUrl The base URL of the Ditto API (e.g., "https://your-app-id.cloud.ditto.live/api/v4/")
     * @param apiKey The API key for authentication
     */
    public DittoHttpClient(String baseUrl, String apiKey) {
        // Ensure the base URL ends with a slash for proper path concatenation
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .create();
        
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
    public List<Ledger> findAccessibleLedgers(String userId) throws IOException {
        try {
            // Build the DQL query with parameters
            String query = String.format(
                "SELECT * FROM %s WHERE array_contains(readerIds, :userId) OR array_contains(writerIds, :userId)",
                Collection.LEDGERS.getName()
            );
            
            // Create JSON request body
            QueryRequest requestBody = new QueryRequest(query, Map.of("userId", userId));
            QueryResponse<Ledger, String> queryResponse = 
                sendQueryRequest(requestBody, Ledger.class, String.class);
            
            return queryResponse.getItems();
            
        } catch (Exception e) {
            throw new IOException("Failed to query Ditto API: " + e.getMessage(), e);
        }
    }

    public Ledger createLedger(LedgerConfig config) throws IOException {
        // TODO Create account, and then ledger. Use the DQL API because legacy /store/write request doesn't support
        // MAP field, which is needed for channels.
        try {
            // Build the DQL query with parameters
            final String query = "INSERT INTO Ledgers VALUES (:ledger)";
            
            // Create JSON request body
            final Ledger ledger = new Ledger();
            ledger.setId(generateId());
            ledger.setCreatedAt(Instant.now());
            ledger.setUpdatedAt(Instant.now());
            ledger.setName(config.getName());
            ledger.setReaderIds(config.getReaderIds());
            ledger.setWriterIds(config.getWriterIds());
            ledger.setNote(config.getNote());
            ledger.setExternalAccountId(config.getExternalAccountId());
            ledger.setCurrencyId(config.getCurrencyId());
            QueryRequest requestBody = new QueryRequest(query, Map.of("ledger", ledger));
            sendQueryRequest(requestBody, Ledger.class, String.class);
            return ledger;
        } catch (Exception e) {
            throw new IOException("Failed to create ledger: " + e.getMessage(), e);
        }
    }

    public Account createAccount(AccountConfig config) throws IOException {
        try {
            // Build the DQL query with parameters
            final String query = "INSERT INTO Accounts VALUES (:account)";
            
            // Create JSON request body
            final Account account = new Account();
            account.setId(generateId());
            account.setCreatedAt(Instant.now());
            account.setUpdatedAt(Instant.now());
            account.setName(config.getName());
            account.setType(config.getType());
            account.setArchived(config.isArchived());
            account.setGroupId(config.getGroupId());
            account.setCurrencyId(config.getCurrencyId());
            account.setAutoClear(config.isAutoClear());
            account.setChannels(config.getChannels());
            QueryRequest requestBody = new QueryRequest(query, Map.of("account", account));
            sendQueryRequest(requestBody, Account.class, String.class);
            return account;
        } catch (Exception e) {
            throw new IOException("Failed to create account: " + e.getMessage(), e);
        }
    }

    private <Item, ItemId> QueryResponse<Item, ItemId> sendQueryRequest(
        QueryRequest request, Class<Item> itemClass, Class<ItemId> itemIdClass) throws IOException {
        try {
            // Create JSON request body
            String jsonRequestBody = gson.toJson(request);
            
            // Create and execute the request
            HttpPost httpPost = new HttpPost(baseUrl + "store/execute");
            httpPost.setEntity(new StringEntity(jsonRequestBody, ContentType.APPLICATION_JSON));
            
            // Execute the request
            String responseBody = sendRequest(httpPost);

            // Parse and return response
            QueryResponse<Item, ItemId> queryResponse = gson.fromJson(
                responseBody,
                TypeToken.getParameterized(QueryResponse.class, itemClass, itemIdClass).getType()
            );
            return queryResponse;
        } catch (Exception e) {
            throw new IOException("Failed to send execute request: " + e.getMessage(), e);
        }
    }

    private String sendRequest(ClassicHttpRequest request) throws IOException {
        try {
            // Execute the request
            String responseBody = httpClient.execute(request, response -> {
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                    return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                } else {
                    throw new IOException("Request failed with status code " + statusCode);
                }
            });
            return responseBody;
        } catch (Exception e) {
            throw new IOException("Failed to send execute request: " + e.getMessage(), e);
        }
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
