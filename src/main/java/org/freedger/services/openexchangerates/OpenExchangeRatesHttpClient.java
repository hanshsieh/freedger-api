package org.freedger.services.openexchangerates;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;
import org.freedger.services.openexchangerates.models.HistoricalRatesRequest;
import org.freedger.services.openexchangerates.models.HistoricalRatesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for interacting with OpenExchangeRates HTTP API.
 *
 * <p>This client provides access to historical exchange rate data from the OpenExchangeRates API.
 * Currently supports the /historical/*.json endpoint.
 *
 * @see <a href="https://docs.openexchangerates.org">OpenExchangeRates API Documentation</a>
 */
public class OpenExchangeRatesHttpClient {
  private static final Logger logger = LoggerFactory.getLogger(OpenExchangeRatesHttpClient.class);
  private static final Timeout REQUEST_TIMEOUT = Timeout.ofSeconds(10);
  private static final Timeout RESPONSE_TIMEOUT = Timeout.ofSeconds(10);
  private static final String DEFAULT_BASE_URL = "https://openexchangerates.org/api/";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final URI baseUri;
  private final String appId;
  private final Gson gson;
  private final CloseableHttpClient httpClient;

  /**
   * Creates a new OpenExchangeRatesHttpClient with the default base URL.
   *
   * @param appId The App ID for authentication
   */
  public OpenExchangeRatesHttpClient(String appId) {
    this(DEFAULT_BASE_URL, appId);
  }

  /**
   * Creates a new OpenExchangeRatesHttpClient with a custom base URL.
   *
   * @param baseUrl The base URL of the OpenExchangeRates API
   * @param appId The App ID for authentication
   */
  public OpenExchangeRatesHttpClient(String baseUrl, String appId) {
    try {
      this.baseUri = new URI(baseUrl);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid base URL: " + baseUrl, e);
    }

    this.appId = appId;
    this.gson = new GsonBuilder().create();

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
      logger.debug("Fetching historical rates from: {}", uri);

      // Create and execute the GET request
      final HttpGet httpGet = new HttpGet(uri);
      HistoricalRatesResponse response = sendRequest(httpGet, HistoricalRatesResponse.class);

      logger.debug("Successfully fetched historical rates for date: {}", dateStr);
      return response;

    } catch (URISyntaxException e) {
      logger.error("Failed to build URI for date: {}", request.getDate(), e);
      throw new IOException("Failed to build URI", e);
    } catch (Exception e) {
      logger.error("Failed to fetch historical rates for date: {}", request.getDate(), e);
      throw new IOException("Failed to fetch historical rates", e);
    }
  }

  /**
   * Sends an HTTP request, parses the response, and returns the parsed object.
   *
   * @param <T> The type of the response object
   * @param request The HTTP request to send
   * @param responseClass The class of the response object
   * @return The parsed response object
   * @throws IOException If the request fails, returns a non-2xx status code, or parsing fails
   */
  private <T> T sendRequest(ClassicHttpRequest request, Class<T> responseClass) throws IOException {
    try {
      return httpClient.execute(
          request,
          response -> {
            int statusCode = response.getCode();
            String responseBody =
                EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (statusCode >= 200 && statusCode < 300) {
              // Parse the response body to the specified class
              return gson.fromJson(responseBody, responseClass);
            } else {
              logger.error("Request failed with status code {}: {}", statusCode, responseBody);
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
