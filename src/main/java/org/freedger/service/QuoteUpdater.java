package org.freedger.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
 

import org.freedger.config.Config;
import org.freedger.repository.ditto.DittoClient;
import org.freedger.repository.ditto.exceptions.DittoException;
import org.freedger.repository.ditto.models.CreateCurrencyRequest;
import org.freedger.repository.ditto.models.CreateCurrencyResponse;
import org.freedger.repository.ditto.models.CreateQuoteRequest;
import org.freedger.repository.ditto.models.Currency;
import org.freedger.repository.ditto.models.CurrencyType;
import org.freedger.repository.ditto.models.DittoResponse;
import org.freedger.repository.ditto.models.QueryCurrenciesRequest;
import org.freedger.repository.ditto.models.QueryQuotesRequest;
import org.freedger.repository.ditto.models.Quote;
import org.freedger.repository.ditto.models.QuoteOrder;
import org.freedger.repository.ditto.models.UpdateInstrumentRequest;
import org.freedger.repository.ditto.models.InstrumentCategory;
import org.freedger.repository.openexchangerates.OpenExchangeRatesClient;
import org.freedger.repository.openexchangerates.models.HistoricalRatesRequest;
import org.freedger.service.models.CurrencyState;
import org.freedger.service.models.OXRConfig;
import org.freedger.service.models.OXRCurrency;
import org.freedger.service.models.OXRCurrencyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuoteUpdater {
  private static final Logger logger = LoggerFactory.getLogger(QuoteUpdater.class);
  private static final String SOURCE = "openexchangerates.org";
  private static final LocalDate EARLIEST_DATE = LocalDate.of(1999, 1, 1);

  private final OpenExchangeRatesClient exchangeRatesClient;
  private final DittoClient dittoClient;
  /**
   * Resource path to the config file.
   */
  private final String configPath;
  
  // Cached per-run state
  private OXRConfig oxrConfig;
  private final Map<String, CurrencyState> stateByCode = new HashMap<>();
  private String transactionId;
  private String quoteCurrencyId;
  private String quoteCurrencyCode;

  public QuoteUpdater(Config config, OpenExchangeRatesClient openExchangeRatesClient, DittoClient dittoClient) {
    this.configPath = config.openExchangeRatesConfigPath();
    this.exchangeRatesClient = openExchangeRatesClient;
    this.dittoClient = dittoClient;
  }
  /**
   * Updates the quotes of all the currencies for the given number of days.
   * 
   * @param maxDays The maximum number of days to update the quotes for.
   */
  synchronized public void updateQuotes(int maxDays) throws IOException {
    if (maxDays <= 0) {
      return;
    }

    try {
      resetState();
      initializeState();
      for (var updatedDays = 0; updatedDays < maxDays; updatedDays++) {
        if (stateByCode.isEmpty()) {
          break;
        }
        final var day = getNextDayToUpdate();
        if (day.isEmpty()) {
          break;
        }
        final var quotes = fetchQuotes(day.get());
        updateQuotesForDay(quotes, day.get());
      }
    } catch (Exception e) {
      logger.error("Failed to update quotes", e);
      throw new IOException("Failed to update quotes", e);
    } finally {
      resetState();
    }
  }

  private void initializeState() throws IOException {
    oxrConfig = loadConfig();
    quoteCurrencyCode = oxrConfig.getQuoteCurrency();
    resolveQuoteCurrencyId();
    Map<String, Currency> dbCurrencies = loadDbCurrencies();
    buildStates(dbCurrencies);
  }

  private void resetState() {
    stateByCode.clear();
    transactionId = null;
    quoteCurrencyId = null;
    quoteCurrencyCode = null;
    oxrConfig = null;
  }

  private void resolveQuoteCurrencyId() throws IOException {
    DittoResponse<List<Currency>> usdQueryResp = dittoClient.queryCurrencies(
      QueryCurrenciesRequest.builder()
        .transactionId(transactionId)
        .ledgerId(null)
        .code(quoteCurrencyCode)
        .build());
    this.transactionId = usdQueryResp.getTransactionId();
    if (usdQueryResp.getData().isEmpty()) {
      throw new IOException(String.format("Quote currency %s not found in Ditto store", quoteCurrencyCode));
    }
    if (usdQueryResp.getData().size() > 1) {
      throw new IOException(String.format("Multiple %s currencies found in Ditto store", quoteCurrencyCode));
    }
    Currency usdCurrency = usdQueryResp.getData().get(0);
    this.quoteCurrencyId = Objects.requireNonNull(usdCurrency.getId()).getId();
  }

  private Map<String, Currency> loadDbCurrencies() throws IOException {
    DittoResponse<List<Currency>> allCurResp = dittoClient.queryCurrencies(
      QueryCurrenciesRequest.builder()
        .transactionId(transactionId)
        .ledgerId(null)
        .build());
    this.transactionId = allCurResp.getTransactionId();
    Map<String, Currency> map = new HashMap<>();
    for (Currency c : allCurResp.getData()) {
      map.put(c.getCode(), c);
    }
    return map;
  }

  private void buildStates(Map<String, Currency> dbCurrencies) throws IOException {
    for (OXRCurrency currencyConf : oxrConfig.getBaseCurrencies()) {
      if (!currencyConf.isEnabled()) {
        continue;
      }
      String code = currencyConf.getCode();
      if (quoteCurrencyCode.equalsIgnoreCase(code)) {
        continue;
      }
      Currency dbCurrency = dbCurrencies.get(code);
      CurrencyType currencyType = mapCurrencyType(currencyConf.getType());
      CurrencyState state = CurrencyState.builder()
        .code(code)
        .name(currencyConf.getName())
        .decimalPlaces(currencyConf.getDecimalPlaces())
        .currencyType(currencyType)
        .instrumentId(dbCurrency != null ? dbCurrency.getInstrumentId() : null)
        .build();
      stateByCode.put(code, state);
      if (state.getInstrumentId() != null) {
        populateStateBoundaries(state);
      }
    }
  }

  private void populateStateBoundaries(CurrencyState state) throws IOException {
    state.setEarliestDate(null);
    state.setEarliestInstant(null);
    state.setLatestDate(null);
    state.setLatestInstant(null);
    // earliest
    DittoResponse<List<Quote>> earliestResp = dittoClient.queryQuotes(
      QueryQuotesRequest.builder()
        .transactionId(transactionId)
        .instrumentId(state.getInstrumentId())
        .order(QuoteOrder.TIME_ASC)
        .limit(1)
        .build());
    this.transactionId = earliestResp.getTransactionId();
    if (!earliestResp.getData().isEmpty()) {
      Instant time = earliestResp.getData().get(0).getTime();
      state.setEarliestInstant(time);
      state.setEarliestDate(time.atOffset(ZoneOffset.UTC).toLocalDate());
    }

    // latest
    DittoResponse<List<Quote>> latestResp = dittoClient.queryQuotes(
      QueryQuotesRequest.builder()
        .transactionId(transactionId)
        .instrumentId(state.getInstrumentId())
        .order(QuoteOrder.TIME_DESC)
        .limit(1)
        .build());
    this.transactionId = latestResp.getTransactionId();
    if (!latestResp.getData().isEmpty()) {
      Instant time = latestResp.getData().get(0).getTime();
      state.setLatestInstant(time);
      state.setLatestDate(time.atOffset(ZoneOffset.UTC).toLocalDate());
    }
  }

  /**
   * Returns the next day to update the quotes for.
   * We cannot calculate all the days in advance because the states of the currencies
   * will be updated as we go along, and the next day to update will depend on the states.
   * For example, currency A has no quotes, and currency B has some quotes from T1 to T2. 
   * We will update the quotes for A and B for T2 + 1. If T2 + 1 is the last stable date,
   * the next day to update should be T2 because A's earliest date will be T2 + 1.
   * 
   * @return The next day to update the quotes for, or empty if no day is available.
   */
  private Optional<LocalDate> getNextDayToUpdate() {
    LocalDate nowUtcDate = LocalDate.now(ZoneOffset.UTC);
    LocalDate lastStableDate = nowUtcDate.minusDays(1);
    if (lastStableDate.isBefore(EARLIEST_DATE)) {
      return Optional.empty();
    }

    boolean hasAnyLatest = stateByCode.values().stream().anyMatch(s -> s.getLatestDate() != null);
    if (!hasAnyLatest) {
      return Optional.of(lastStableDate);
    }
    // Forward first
    LocalDate forwardStart = null;
    for (CurrencyState state : stateByCode.values()) {
      if (state.getLatestDate() != null) {
        LocalDate start = state.getLatestDate().plusDays(1);
        if (forwardStart == null || start.isBefore(forwardStart)) {
          forwardStart = start;
        }
      }
    }
    if (forwardStart != null && !forwardStart.isAfter(lastStableDate)) {
      return Optional.of(forwardStart);
    }

    LocalDate backwardStart = null;
    for (CurrencyState state : stateByCode.values()) {
      if (state.getEarliestDate() != null) {
        LocalDate date = state.getEarliestDate();
        if (backwardStart == null || date.isAfter(backwardStart)) {
          backwardStart = date;
        }
      }
    }
    if (backwardStart != null && !backwardStart.isBefore(EARLIEST_DATE)) {
      return Optional.of(backwardStart);
    }
    return Optional.empty();
  }

  private Map<String, Double> fetchQuotes(LocalDate day) throws IOException {
    final var resp =exchangeRatesClient.getHistoricalRates(HistoricalRatesRequest.builder()
      .date(day)
      // The OpenExchangeRates API returns the rates for the given base currency.
      // We will need to take an inverse of the returned rates.
      .base(quoteCurrencyCode)
      .build());
    final var rates = resp.getRates();
    final var invertedRates = new HashMap<String, Double>();
    rates.forEach((code, rate) -> {
      if (rate.doubleValue() <= 0.0) {
        return;
      }
      invertedRates.put(code, 1.0 / rate);
    });
    return invertedRates;
  }

  private void updateQuotesForDay(Map<String, Double> quotes, LocalDate day) throws IOException, DittoException {
    final var dayInstant = day.atStartOfDay().toInstant(ZoneOffset.UTC);
    Set<String> disableCodes = new HashSet<>();
    for (final CurrencyState state : stateByCode.values()) {
      final var code = state.getCode();
      if (!shouldInsertQuote(state, dayInstant)) {
        continue;
      }
      
      final var quote = quotes.get(code);
      if (quote == null) {
        // Skip the update for this currency
        disableCodes.add(code);
        logger.warn("Quote for {} is not available for day {}", code, day);
        continue;
      }

      if (state.getInstrumentId() == null) {
        createCurrency(state, quote);
        // After creation, boundaries become this day
        state.setEarliestInstant(dayInstant);
        state.setLatestInstant(dayInstant);
        state.setEarliestDate(day);
        state.setLatestDate(day);
      } else if (state.getEarliestInstant() == null || dayInstant.isBefore(state.getEarliestInstant())) {
        updateInstrumentInitialQuote(state, quote);
        state.setEarliestInstant(dayInstant);
        state.setEarliestDate(day);
      }
      if (state.getLatestInstant() == null || dayInstant.isAfter(state.getLatestInstant())) {
        state.setLatestInstant(dayInstant);
        state.setLatestDate(day);
      }

      createQuote(state, dayInstant, quote);
    }
    for (String code : disableCodes) {
      stateByCode.remove(code);
    }
  }

  private boolean shouldInsertQuote(CurrencyState st, Instant dayInstant) {
    if (st.getEarliestInstant() == null && st.getLatestInstant() == null) {
      return true;
    }
    if (st.getEarliestInstant() != null && dayInstant.isBefore(st.getEarliestInstant())) {
      return true;
    }
    if (st.getLatestInstant() != null && dayInstant.isAfter(st.getLatestInstant())) {
      return true;
    }
    return false;
  }

  private void createCurrency(CurrencyState state, double quote) throws IOException {
    CurrencyType currencyType = state.getCurrencyType();
    String symbol = String.format("%s/%s", state.getCode(), quoteCurrencyCode);
    DittoResponse<CreateCurrencyResponse> resp = dittoClient.createCurrency(
      CreateCurrencyRequest.builder()
        .transactionId(transactionId)
        .ledgerId(null)
        .type(currencyType)
        .code(state.getCode())
        .name(state.getName())
        .symbol(symbol)
        .decimals(state.getDecimalPlaces())
        .category(mapInstrumentCategory(currencyType))
        .quoteCurrencyId(quoteCurrencyId)
        .initialQuote(BigDecimal.valueOf(quote))
        .build());
    this.transactionId = resp.getTransactionId();
    state.setInstrumentId(resp.getData().getInstrumentId());
    logger.info("Created currency {} with instrument {}. Initial quote: {}", state.getCode(), state.getInstrumentId(), quote);
  }

  private void updateInstrumentInitialQuote(CurrencyState state, double rate) throws IOException, DittoException {
    String symbol = String.format("%s/%s", state.getCode(), quoteCurrencyCode);
    DittoResponse<String> updateResp = dittoClient.updateInstrument(
      UpdateInstrumentRequest.builder()
        .transactionId(transactionId)
        .ledgerId(null)
        .instrumentId(state.getInstrumentId())
        .symbol(symbol)
        .name(state.getName())
        .category(mapInstrumentCategory(state.getCurrencyType()))
        .decimals(state.getDecimalPlaces())
        .quoteCurrencyId(quoteCurrencyId)
        .initialQuote(BigDecimal.valueOf(rate))
        .build());
    this.transactionId = updateResp.getTransactionId();
    logger.info("Updated instrument {} of currency {}. Initial quote: {}", state.getInstrumentId(), state.getCode(), rate);
  }

  private void createQuote(CurrencyState state, Instant time, double quote) throws IOException {
    DittoResponse<String> createQuoteResp = dittoClient.createQuote(
      CreateQuoteRequest.builder()
        .transactionId(transactionId)
        .ledgerId(null)
        .instrumentId(state.getInstrumentId())
        .time(time)
        .value(BigDecimal.valueOf(quote))
        .source(SOURCE)
        .build());
    this.transactionId = createQuoteResp.getTransactionId();
    logger.info("Created quote for currency {}. Instrument: {}, Time: {}, Value: {}",
      state.getCode(), state.getInstrumentId(), time, quote);
  }

  private OXRConfig loadConfig() throws IOException {
    try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(configPath)) {
      if (is == null) {
        throw new IOException("Config not found at resource path: " + configPath);
      }
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(is, OXRConfig.class);
    }
  }

  private static CurrencyType mapCurrencyType(OXRCurrencyType type) {
    switch (type) {
      case FIAT:
        return CurrencyType.FIAT;
      case CRYPTO:
        return CurrencyType.CRYPTO;
      default:
        throw new IllegalArgumentException("Invalid currency type: " + type);
    }
  }

  private static InstrumentCategory mapInstrumentCategory(CurrencyType type) {
    switch (type) {
      case FIAT:
        return InstrumentCategory.FOREX;
      case CRYPTO:
        return InstrumentCategory.CRYPTO;
      default:
        return InstrumentCategory.FOREX;
    }
  }
}
