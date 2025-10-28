package org.freedger.repository.openexchangerates;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Level;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;
import org.freedger.controller.utils.AppContext;
import org.freedger.repository.openexchangerates.models.CurrenciesRequest;
import org.freedger.repository.openexchangerates.models.HistoricalRatesRequest;
import org.freedger.repository.openexchangerates.models.HistoricalRatesResponse;

/**
 * Client for interacting with OpenExchangeRates HTTP API.
 *
 * <p>This client provides access to historical exchange rate data and currency information from the
 * OpenExchangeRates API. Currently supports:
 * <ul>
 *   <li>/historical/*.json - Historical exchange rates</li>
 *   <li>/currencies.json - List of all available currencies</li>
 * </ul>
 *
 * @see <a href="https://docs.openexchangerates.org">OpenExchangeRates API Documentation</a>
 */
public class OpenExchangeRatesClient {
  private static final Timeout REQUEST_TIMEOUT = Timeout.ofSeconds(10);
  private static final Timeout RESPONSE_TIMEOUT = Timeout.ofSeconds(10);
  private static final String DEFAULT_BASE_URL = "https://openexchangerates.org/api/";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final URI baseUri;
  private final String appId;
  private final ObjectMapper objectMapper;
  private final CloseableHttpClient httpClient;

  /**
   * Creates a new OpenExchangeRatesHttpClient with the default base URL.
   *
   * @param appId The App ID for authentication
   */
  public OpenExchangeRatesClient(String appId) {
    this(DEFAULT_BASE_URL, appId);
  }

  /**
   * Creates a new OpenExchangeRatesHttpClient with a custom base URL.
   *
   * @param baseUrl The base URL of the OpenExchangeRates API
   * @param appId The App ID for authentication
   */
  public OpenExchangeRatesClient(String baseUrl, String appId) {
    try {
      this.baseUri = new URI(baseUrl);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid base URL: " + baseUrl, e);
    }

    this.appId = appId;
    this.objectMapper = new ObjectMapper();

    // Create a reusable HttpClient with connection pooling and timeouts
    RequestConfig config =
        RequestConfig.custom()
            .setConnectionRequestTimeout(REQUEST_TIMEOUT)
            .setResponseTimeout(RESPONSE_TIMEOUT)
            .build();

    this.httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
  }

  /**
   * Gets historical exchange rates based on the provided request.
   *
   * <p>Returns the last exchange rate values published for the given UTC date (up to and including
   * 23:59:59 UTC), except for the current UTC date which returns the most recent rates available.
   *
   * <p>Note: Historical rates are available from 1st January 1999 onwards.
   *
   * <p>Note: Changing the base currency and requesting specific symbols require a Developer,
   * Enterprise or Unlimited plan.
   *
   * <p>Example usage:
   *
   * <pre>
   * HistoricalRatesRequest request = HistoricalRatesRequest.builder()
   *     .date(LocalDate.of(2024, 1, 1))
   *     .base("EUR")
   *     .symbols("USD", "GBP", "JPY")
   *     .build();
   * HistoricalRatesResponse response = client.getHistoricalRates(request);
   * </pre>
   *
   * @param request The request containing date and optional parameters
   * @return Historical exchange rates for the specified date
   * @throws IOException If an error occurs while fetching the rates
   */
  public HistoricalRatesResponse getHistoricalRates(HistoricalRatesRequest request)
      throws IOException {
    try {
      // Format the date
      String dateStr = request.getDate().format(DATE_FORMATTER);
      String fileName = dateStr + ".json";

      // Build the URI with query parameters using URIBuilder
      URIBuilder uriBuilder = new URIBuilder(baseUri);
      uriBuilder.appendPathSegments("historical", fileName);

      // Add required app_id parameter
      uriBuilder.addParameter("app_id", appId);

      // Add optional base currency parameter
      if (request.getBase() != null) {
        uriBuilder.addParameter("base", request.getBase());
      }

      // Add optional symbols parameter
      if (request.getSymbols() != null) {
        String symbolsStr = String.join(",", request.getSymbols());
        uriBuilder.addParameter("symbols", symbolsStr);
      }

      final URI uri = uriBuilder.build();
      AppContext.log(Level.FINE, "Fetching historical rates from: {0}", uri);

      // Create and execute the GET request
      final HttpGet httpGet = new HttpGet(uri);
      HistoricalRatesResponse response = sendRequest(httpGet, new TypeReference<HistoricalRatesResponse>() {});

      AppContext.log(Level.FINE, "Successfully fetched historical rates for date: {0}", dateStr);
      return response;

    } catch (Exception e) {
      AppContext.log(Level.SEVERE, e, "Failed to fetch historical rates for date: {0}", request.getDate());
      throw new IOException("Failed to fetch historical rates", e);
    }
  }

  /**
   * Gets a list of all currency symbols available from the Open Exchange Rates API.
   *
   * <p>This endpoint returns a JSON object where each key:value pair represents a currency's symbol
   * and unit display name (singular and Capitalised).
   *
   * <p>Note: Requests to currencies.json do not count towards your account usage limit, and App ID
   * authentication is optional for this endpoint.
   *
   * <p>Example usage:
   *
   * <pre>
   * // Get all standard currencies
   * CurrenciesResponse response = client.getCurrencies(CurrenciesRequest.create());
   *
   * // Get all currencies including alternative/black market rates
   * CurrenciesRequest request = CurrenciesRequest.builder()
   *     .showAlternative(true)
   *     .build();
   * CurrenciesResponse response = client.getCurrencies(request);
   * </pre>
   *
   * @param request The request with optional parameters for alternative currencies
   * @return A map of currency codes to their full display names
   * @throws IOException If an error occurs while fetching the currencies
   */
  public Map<String, String> getCurrencies(CurrenciesRequest request) throws IOException {
    try {
      // Build the URI with query parameters using URIBuilder
      URIBuilder uriBuilder = new URIBuilder(baseUri);
      uriBuilder.appendPathSegments("currencies.json");
      uriBuilder.addParameter("show_alternative", Boolean.toString(request.isShowAlternative()));
      uriBuilder.addParameter("show_inactive", Boolean.toString(request.isShowInactive()));

      final URI uri = uriBuilder.build();

      final HttpGet httpGet = new HttpGet(uri);
      
      return sendRequest(httpGet, new TypeReference<Map<String, String>>() {});
    } catch (Exception e) {
      AppContext.log(Level.SEVERE, e, "Failed to fetch currencies");
      throw new IOException("Failed to fetch currencies", e);
    }
  }

  /**
   * Sends an HTTP request, parses the response, and returns the parsed object.
   *
   * @param <T> The type of the response object
   * @param request The HTTP request to send
   * @param responseType The type reference for the response object
   * @return The parsed response object
   * @throws IOException If the request fails, returns a non-2xx status code, or parsing fails
   */
  private <T> T sendRequest(ClassicHttpRequest request, TypeReference<T> responseType) throws IOException {
    try {
      return httpClient.execute(
          request,
          response -> {
            int statusCode = response.getCode();
            String responseBody =
                EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode >= 200 && statusCode < 300) {
              // Parse the response body to the specified class
              return objectMapper.readValue(responseBody, responseType);
            } else {
              AppContext.log(Level.SEVERE, "Request failed with status code {0}: {1}", statusCode, responseBody);
              throw new IOException(
                  "Request failed with status code " + statusCode + ": " + responseBody);
            }
          });
    } catch (Exception e) {
      throw new IOException("Failed to send request", e);
    }
  }

  /**
   * Closes the HTTP client and releases any resources.
   *
   * @throws IOException If an error occurs while closing the client
   */
  public void close() throws IOException {
    httpClient.close();
  }
}
