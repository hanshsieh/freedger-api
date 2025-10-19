package org.freedger.services.ditto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;
import org.freedger.services.ditto.exceptions.DittoNotFoundException;
import org.freedger.services.ditto.models.Account;
import org.freedger.services.ditto.models.AccountType;
import org.freedger.services.ditto.models.CreateLedgerRequest;
import org.freedger.services.ditto.models.Currency;
import org.freedger.services.ditto.models.DittoResponse;
import org.freedger.services.ditto.models.GetLedgerRequest;
import org.freedger.services.ditto.models.Ledger;
import org.freedger.services.ditto.models.LedgerChildId;
import org.freedger.services.ditto.models.QueryCurrenciesRequest;
import org.freedger.services.ditto.models.QueryRequest;
import org.freedger.services.ditto.models.QueryResponse;
import org.freedger.services.ditto.models.UpdateLedgerRequest;
import org.freedger.services.ditto.models.UpsertCommand;
import org.freedger.services.ditto.models.WriteCommand;
import org.freedger.services.ditto.models.WriteCommandResult;
import org.freedger.services.ditto.models.WriteRequest;
import org.freedger.services.ditto.models.WriteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client for interacting with Ditto's HTTP API. */
public class DittoClient {
  private static final String HEADER_TXN_ID = "X-DITTO-TXN-ID";
  private static final Logger logger = LoggerFactory.getLogger(DittoClient.class);
  private static final Timeout REQUEST_TIMEOUT = Timeout.ofSeconds(10);
  private static final Timeout RESPONSE_TIMEOUT = Timeout.ofSeconds(10);

  private final URI baseUri;
  private final Gson gson;
  private final CloseableHttpClient httpClient;

  /**
   * Creates a new DittoHttpClient with the specified base URL and API key.
   *
   * @param baseUrl The base URL of the Ditto API (e.g.,
   *     "https://your-app-id.cloud.ditto.live/api/v4/")
   * @param apiKey The API key for authentication
   */
  public DittoClient(String baseUrl, String apiKey) {
    try {
      this.baseUri = new URI(baseUrl);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid base URL: " + baseUrl, e);
    }

    this.gson =
        new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .registerTypeAdapter(BigDecimal.class, new BigDecimalAdapter())
            .create();

    // Create a reusable HttpClient with connection pooling and timeouts
    RequestConfig config =
        RequestConfig.custom()
            .setConnectionRequestTimeout(REQUEST_TIMEOUT)
            .setResponseTimeout(RESPONSE_TIMEOUT)
            .build();

    this.httpClient =
        HttpClients.custom()
            .setDefaultRequestConfig(config)
            .setDefaultHeaders(
                Arrays.asList(new Header[] {new BasicHeader("Authorization", "Bearer " + apiKey)}))
            .build();
  }

  /**
   * Query ledgers where the user has read or write access.
   *
   * @param userId The user ID to check access for
   * @param transactionId The transaction ID to use for the request
   * @return List of ledgers the user can access
   */
  public List<Ledger> queryLedgers(String userId, String transactionId) throws IOException {
    try {
      // Build the DQL query with parameters
      String query =
          String.format(
              "SELECT * FROM %s WHERE schemaVersion = :schemaVersion AND "
                  + "(array_contains(readerIds, :userId) OR array_contains(writerIds, :userId))",
              Collection.LEDGERS.getName());
      final Map<String, Object> args =
          Map.of("userId", userId, "schemaVersion", Ledger.SCHEMA_VERSION);

      // Create JSON request body
      QueryRequest requestBody = new QueryRequest(query, args);
      QueryResponse<Ledger, String> queryResponse =
          sendQueryRequest(requestBody, Ledger.class, String.class, transactionId);

      return queryResponse.getItems();

    } catch (Exception e) {
      throw new IOException("Failed to query Ditto API: " + e.getMessage(), e);
    }
  }

  public DittoResponse<Ledger> getLedger(GetLedgerRequest request) throws IOException {
    try {
      final var query =
          String.format(
              "SELECT * FROM %s WHERE _id = :id AND schemaVersion = :schemaVersion AND"
                  + " (array_contains(writerIds, :userId) OR array_contains(readerIds, :userId))"
                  + " LIMIT 1",
              Collection.LEDGERS.getName());
      final Map<String, Object> args =
          Map.of(
              "id", request.getId(),
              "schemaVersion", Ledger.SCHEMA_VERSION,
              "userId", request.getUserId());
      final var response =
          sendQueryRequest(
              new QueryRequest(query, args),
              Ledger.class,
              String.class,
              request.getTransactionId());
      if (response.getItems().isEmpty()) {
        throw new DittoNotFoundException("No ledger found with ID: " + request.getId());
      }
      final var ledger = response.getItems().get(0);
      final var transactionId = String.valueOf(response.getTransactionId());
      return new DittoResponse<Ledger>(transactionId, ledger);
    } catch (Exception e) {
      throw new IOException("Failed to get ledger with ID: " + request.getId(), e);
    }
  }

  /**
   * Creates a new ledger. A default external account will also be created for the ledger. The
   * ledger and the account are created atomically.
   *
   * @param config The ledger configuration
   * @return The created ledger
   * @throws IOException If an error occurs while creating the ledger
   */
  public DittoResponse<Ledger> createLedger(CreateLedgerRequest config) throws IOException {
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
      account.setArchivedAt(null);
      account.setGroupId(null);
      account.setCurrencyId(config.getCurrencyId());
      account.setOpeningBalance(BigDecimal.ZERO);
      account.setAutoReconcile(true);
      account.setNote("");
      account.setOrder(0.0);

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

      final var writeResponse = sendWriteRequest(request);
      logger.info("Created ledger. Ledger ID: {}, Account ID: {}", ledgerId, accountId);
      // For the /store/write request, ID mustn't be set in the "value", so we need to set it
      // after sending the request.
      ledger.setId(ledgerId);
      final var transactionId = getTransactionId(writeResponse);
      return new DittoResponse<>(transactionId.orElse(null), ledger);
    } catch (Exception e) {
      throw new IOException("Failed to create ledger", e);
    }
  }

  public DittoResponse<String> updateLedger(UpdateLedgerRequest request)
      throws IOException, DittoNotFoundException {
    try {
      final var now = Instant.now();
      final StringBuilder queryBuilder = new StringBuilder();
      queryBuilder.append(String.format("UPDATE %s", Collection.LEDGERS.getName()));
      final List<String> setClauses =
          new ArrayList<>() {
            {
              add("updatedAt = :updatedAt");
              add("name = :name");
              add("note = :note");
              add("currencyId = :currencyId");
              add("externalAccountId = :externalAccountId");
              add("readerIds = :readerIds");
              add("writerIds = :writerIds");
            }
          };
      final Map<String, Object> args =
          new HashMap<>() {
            {
              put("schemaVersion", Ledger.SCHEMA_VERSION);
              put("updatedAt", now);
              put("userId", request.getUserId());
              put("ledgerId", request.getId());
              put("name", request.getName());
              put("note", request.getNote());
              put("currencyId", request.getCurrencyId());
              put("externalAccountId", request.getExternalAccountId());
              put("readerIds", request.getReaderIds());
              put("writerIds", request.getWriterIds());
            }
          };
      queryBuilder.append(" SET ");
      queryBuilder.append(String.join(", ", setClauses));
      queryBuilder.append(
          " WHERE _id = :ledgerId AND schemaVersion = :schemaVersion AND array_contains(writerIds,"
              + " :userId)");

      // Create JSON request body
      QueryRequest queryRequest = new QueryRequest(queryBuilder.toString(), args);

      final var response =
          sendQueryRequest(queryRequest, Object.class, String.class, request.getTransactionId());
      final var mutatedIds = response.getMutatedDocumentIds();
      if (mutatedIds.isEmpty()) {
        throw new DittoNotFoundException("No ledger found with ID: " + request.getId());
      }
      logger.info("Updated ledger. Ledger ID: {}", request.getId());
      return new DittoResponse<String>(
          String.valueOf(response.getTransactionId()), request.getId());
    } catch (DittoNotFoundException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException("Failed to update ledger with ID: " + request.getId(), e);
    }
  }

  public List<Currency> queryCurrencies(QueryCurrenciesRequest request) throws IOException {
    try {
      final var queryBuilder =
          new StringBuilder(String.format("SELECT * FROM %s", Collection.CURRENCIES.getName()));
      final List<String> whereClauses =
          new ArrayList<>() {
            {
              add("schemaVersion = :schemaVersion");
            }
          };
      final Map<String, Object> args =
          new HashMap<>() {
            {
              put("schemaVersion", Currency.SCHEMA_VERSION);
            }
          };
      if (request.getLedgerId() != null) {
        whereClauses.add("_id.ledgerId = :ledgerId");
        args.put("ledgerId", request.getLedgerId());
      } else {
        whereClauses.add("_id.ledgerId IS NULL");
      }
      if (request.getType() != null) {
        whereClauses.add("type = :type");
        args.put("type", request.getType());
      }
      if (request.getCode() != null) {
        whereClauses.add("code = :code");
        args.put("code", request.getCode());
      }
      queryBuilder.append(" WHERE ");
      queryBuilder.append(String.join(" AND ", whereClauses));
      final var query = queryBuilder.toString();
      final var response =
          sendQueryRequest(
              new QueryRequest(query, args),
              Currency.class,
              LedgerChildId.class,
              request.getTransactionId());
      return response.getItems();
    } catch (Exception e) {
      throw new IOException("Failed to query currencies", e);
    }
  }

  private Optional<String> getTransactionId(WriteResponse writeResponse) {
    final var transactionId =
        writeResponse.getResults().stream().mapToLong(WriteCommandResult::getTransactionId).max();
    return Optional.ofNullable(
        transactionId.isPresent() ? String.valueOf(transactionId.getAsLong()) : null);
  }

  private <Item, ItemId> QueryResponse<Item, ItemId> sendQueryRequest(
      QueryRequest request, Class<Item> itemClass, Class<ItemId> itemIdClass, String transactionId)
      throws IOException {
    try {
      // Create JSON request body
      String jsonRequestBody = gson.toJson(request);

      // Build URI using URIBuilder
      URIBuilder uriBuilder = new URIBuilder(baseUri);
      uriBuilder.appendPathSegments("store", "execute");
      URI uri = uriBuilder.build();

      // Create and execute the request
      HttpPost httpPost = new HttpPost(uri);
      httpPost.setEntity(new StringEntity(jsonRequestBody, ContentType.APPLICATION_JSON));
      if (transactionId != null) {
        httpPost.setHeader(HEADER_TXN_ID, transactionId);
      }

      // Execute the request
      String responseBody = sendRequest(httpPost);

      // Parse and return response
      QueryResponse<Item, ItemId> queryResponse =
          gson.fromJson(
              responseBody,
              TypeToken.getParameterized(QueryResponse.class, itemClass, itemIdClass).getType());
      return queryResponse;
    } catch (URISyntaxException e) {
      throw new IOException("Failed to build URI", e);
    } catch (Exception e) {
      throw new IOException("Failed to send execute request", e);
    }
  }

  // Sends a `/store/write` request to update, insert, or remove documents using legacy QL.
  // Different from the newer `/store/execute` request, this endpoint supports multiple commands in
  // a single transaction.
  //
  // @param request The request body to send
  // @return The response from the request
  // @see https://docs.ditto.live/cloud/http-api/api/post-storewrite
  private WriteResponse sendWriteRequest(WriteRequest request) throws IOException {
    try {
      // Create JSON request body
      String jsonRequestBody = gson.toJson(request);

      // Build URI using URIBuilder
      URIBuilder uriBuilder = new URIBuilder(baseUri);
      uriBuilder.appendPathSegments("store", "write");
      URI uri = uriBuilder.build();

      // Create and execute the request
      HttpPost httpPost = new HttpPost(uri);
      httpPost.setEntity(new StringEntity(jsonRequestBody, ContentType.APPLICATION_JSON));

      // Execute the request
      String responseBody = sendRequest(httpPost);

      // Parse and return response
      WriteResponse writeResponse = gson.fromJson(responseBody, WriteResponse.class);
      return writeResponse;
    } catch (URISyntaxException e) {
      throw new IOException("Failed to build URI", e);
    } catch (Exception e) {
      throw new IOException("Failed to send write request", e);
    }
  }

  private String sendRequest(ClassicHttpRequest request) throws IOException {
    try {
      // Execute the request
      String responseBody =
          httpClient.execute(
              request,
              response -> {
                int statusCode = response.getCode();
                if (statusCode >= 200 && statusCode < 300) {
                  return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                } else {
                  throw new IOException("Request failed with status code " + statusCode);
                }
              });
      return responseBody;
    } catch (Exception e) {
      throw new IOException("Failed to send request", e);
    }
  }

  private String generateId() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
