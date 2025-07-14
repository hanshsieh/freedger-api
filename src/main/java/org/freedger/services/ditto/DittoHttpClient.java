package org.freedger.services.ditto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.freedger.services.ditto.models.Account;
import org.freedger.services.ditto.models.AccountType;
import org.freedger.services.ditto.models.Ledger;
import org.freedger.services.ditto.models.LedgerChildId;
import org.freedger.services.ditto.models.LedgerCreate;
import org.freedger.services.ditto.models.QueryRequest;
import org.freedger.services.ditto.models.QueryResponse;
import org.freedger.services.ditto.models.UpsertCommand;
import org.freedger.services.ditto.models.WriteRequest;
import org.freedger.services.ditto.models.WriteResponse;
import org.freedger.services.ditto.models.WriteCommand;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client for interacting with Ditto's HTTP API.
 */
public class DittoHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(DittoHttpClient.class);
    private static final Timeout REQUEST_TIMEOUT = Timeout.ofSeconds(10);
    private static final Timeout RESPONSE_TIMEOUT = Timeout.ofSeconds(10);
    
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
            .setConnectionRequestTimeout(REQUEST_TIMEOUT)
            .setResponseTimeout(RESPONSE_TIMEOUT)
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

    /**
     * Creates a new ledger.
     * A default external account will also be created for the ledger.
     * The ledger and the account are created atomically.
     * 
     * @param config The ledger configuration
     * @return The created ledger
     * @throws IOException If an error occurs while creating the ledger
     */
    public Ledger createLedger(LedgerCreate config) throws IOException {
        try {
            final var now = Instant.now();
            WriteRequest request = new WriteRequest();
            List<WriteCommand> commands = new ArrayList<>();
            request.setCommands(commands);

            final var ledgerId = generateId();
            final var accountId = generateId();

            var account = new Account();
            account.setCreatedAt(now);
            account.setUpdatedAt(now);
            account.setName(config.getExternalAccountName());
            account.setType(AccountType.COUNTERPARTY);
            account.setArchived(false);
            account.setGroupId(null);
            account.setCurrencyId(config.getCurrencyId());
            account.setAutoClear(true);
            account.setOrder(1.0);

            var createAccountCommand = new UpsertCommand<LedgerChildId, Account>();
            createAccountCommand.setCollection(Collection.ACCOUNTS.getName());
            var accountCompositeId = new LedgerChildId().setId(accountId).setLedgerId(ledgerId);
            createAccountCommand.setId(accountCompositeId);
            createAccountCommand.setValue(account);
            commands.add(createAccountCommand);

            var ledger = new Ledger();
            ledger.setCreatedAt(now);
            ledger.setUpdatedAt(now);
            ledger.setName(config.getName());
            ledger.setReaderIds(config.getReaderIds());
            ledger.setWriterIds(config.getWriterIds());
            ledger.setNote(config.getNote());
            ledger.setExternalAccountId(accountId);
            ledger.setCurrencyId(config.getCurrencyId());
            
            var createLedgerCommand = new UpsertCommand<String, Ledger>();
            createLedgerCommand.setCollection(Collection.LEDGERS.getName());
            createLedgerCommand.setId(ledgerId);
            createLedgerCommand.setValue(ledger);
            commands.add(createLedgerCommand);

            sendWriteRequest(request);
            logger.info("Created ledger. Ledger ID: {}, Account ID: {}", ledgerId, accountId);
            // For the /store/write request, ID mustn't be set in the "value", so we need to set it
            // after sending the request.
            ledger.setId(ledgerId);
            return ledger;
        } catch (Exception e) {
            throw new IOException("Failed to create ledger", e);
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

    private WriteResponse sendWriteRequest(WriteRequest request) throws IOException {
        try {
            // Create JSON request body
            String jsonRequestBody = gson.toJson(request);
            
            // Create and execute the request
            HttpPost httpPost = new HttpPost(baseUrl + "store/write");
            httpPost.setEntity(new StringEntity(jsonRequestBody, ContentType.APPLICATION_JSON));
            
            // Execute the request
            String responseBody = sendRequest(httpPost);

            // Parse and return response
            WriteResponse writeResponse = gson.fromJson(
                responseBody,
                WriteResponse.class
            );
            return writeResponse;
        } catch (Exception e) {
            throw new IOException("Failed to send write request: " + e.getMessage(), e);
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
