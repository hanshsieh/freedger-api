package org.freedger.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
 

import com.fasterxml.jackson.databind.ObjectMapper;
 

import org.freedger.config.Config;
import org.freedger.repository.ditto.DittoClient;
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
import org.freedger.repository.openexchangerates.models.HistoricalRatesResponse;
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

    initializeConfig();
    resolveQuoteCurrencyId();
    Map<String, Currency> dbCurrencies = loadDbCurrencies();
    buildStates(dbCurrencies);
    List<LocalDate> dayPlan = planDays(maxDays);
    if (dayPlan.isEmpty()) {
      return;
    }
    for (LocalDate day : dayPlan) {
      HistoricalRatesResponse resp = fetchRates(day);
      processDay(resp, day);
    }
  }

  private void initializeConfig() throws IOException {
    oxrConfig = loadConfig();
    stateByCode.clear();
    transactionId = null;
    quoteCurrencyId = null;
    quoteCurrencyCode = oxrConfig.getQuoteCurrency();
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
    for (OXRCurrency conf : oxrConfig.getBaseCurrencies()) {
      if (!conf.isEnabled()) {
        continue;
      }
      String code = conf.getCode();
      if (quoteCurrencyCode.equalsIgnoreCase(code)) {
        continue;
      }
      Currency dbCurrency = dbCurrencies.get(code);
      CurrencyType currencyType = mapCurrencyType(conf.getType());
      CurrencyState state = CurrencyState.builder()
        .code(code)
        .name(conf.getName())
        .decimalPlaces(conf.getDecimalPlaces())
        .enabled(conf.isEnabled())
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

  private List<LocalDate> planDays(int maxDays) {
    LocalDate nowUtcDate = LocalDate.now(ZoneOffset.UTC);
    LocalDate lastStableDate = nowUtcDate.minusDays(1);
    List<LocalDate> plan = new ArrayList<>();

    boolean hasAnyLatest = stateByCode.values().stream().anyMatch(s -> s.getLatestDate() != null);
    if (!hasAnyLatest) {
      LocalDate date = lastStableDate;
      while (!date.isBefore(EARLIEST_DATE) && plan.size() < maxDays) {
        plan.add(date);
        date = date.minusDays(1);
      }
      return plan;
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
      LocalDate date = forwardStart;
      while (!date.isAfter(lastStableDate) && plan.size() < maxDays) {
        plan.add(date);
        date = date.plusDays(1);
      }
    }

    if (plan.size() < maxDays) {
      LocalDate backwardStart = null;
      for (CurrencyState state : stateByCode.values()) {
        if (state.getEarliestDate() != null) {
          LocalDate date = state.getEarliestDate();
          if (backwardStart == null || date.isAfter(backwardStart)) {
            backwardStart = date;
          }
        }
      }
      if (backwardStart != null) {
        LocalDate date = backwardStart;
        while (!date.isBefore(EARLIEST_DATE) && plan.size() < maxDays) {
          if (!plan.contains(date)) {
            plan.add(date);
          }
          date = date.minusDays(1);
        }
      }
    }
    return plan;
  }

  private HistoricalRatesResponse fetchRates(LocalDate day) throws IOException {
    return exchangeRatesClient.getHistoricalRates(HistoricalRatesRequest.builder()
      .date(day)
      .base(quoteCurrencyCode)
      .build());
  }

  private void processDay(HistoricalRatesResponse ratesResp, LocalDate day) throws IOException {
    Map<String, Double> rates = ratesResp.getRates();
    if (rates == null || rates.isEmpty()) {
      return;
    }
    Instant quoteTime = day.atStartOfDay().toInstant(ZoneOffset.UTC);
    for (CurrencyState state : stateByCode.values()) {
      if (!state.isEnabled()) {
        continue;
      }
      String code = state.getCode();
      Instant dayInstant = quoteTime;
      if (!shouldInsertQuote(state, dayInstant)) {
        continue;
      }
      
      Double rate = rates.get(code);
      if (rate == null || rate.doubleValue() <= 0) {
        // Skip the update for this currency
        state.setEnabled(false);
        logger.warn("Rate for {} is not available for day {}", code, day);
        continue;
      }

      if (state.getInstrumentId() == null) {
        createCurrency(state, rate);
        // After creation, boundaries become this day
        state.setEarliestInstant(dayInstant);
        state.setLatestInstant(dayInstant);
        state.setEarliestDate(day);
        state.setLatestDate(day);
      } else if (state.getEarliestInstant() == null || dayInstant.isBefore(state.getEarliestInstant())) {
        updateInstrumentInitialQuote(state, rate);
        state.setEarliestInstant(dayInstant);
        state.setEarliestDate(day);
      }

      createQuote(state, dayInstant, rate);

      if (state.getLatestInstant() == null || dayInstant.isAfter(state.getLatestInstant())) {
        state.setLatestInstant(dayInstant);
        state.setLatestDate(day);
      }
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

  private void createCurrency(CurrencyState state, double rate) throws IOException {
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
        .initialQuote(BigDecimal.valueOf(rate))
        .build());
    this.transactionId = resp.getTransactionId();
    state.setInstrumentId(resp.getData().getInstrumentId());
  }

  private void updateInstrumentInitialQuote(CurrencyState state, double rate) throws IOException {
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
  }

  private void createQuote(CurrencyState st, Instant time, double rate) throws IOException {
    DittoResponse<String> createQuoteResp = dittoClient.createQuote(
      CreateQuoteRequest.builder()
        .transactionId(transactionId)
        .ledgerId(null)
        .instrumentId(st.getInstrumentId())
        .time(time)
        .value(BigDecimal.valueOf(rate))
        .source(SOURCE)
        .build());
    this.transactionId = createQuoteResp.getTransactionId();
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
