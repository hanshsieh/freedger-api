package org.freedger.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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
import org.freedger.service.models.OXRConfig;
import org.freedger.service.models.OXRCurrency;
import org.freedger.service.models.OXRCurrencyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuoteUpdater {
  private static final Logger logger = LoggerFactory.getLogger(QuoteUpdater.class);
  private static final String QUOTE_CURRENCY_CODE = "USD";
  private static final String SOURCE = "openexchangerates.org";
  private static final LocalDate EARLIEST_DATE = LocalDate.of(1999, 1, 1);

  private final OpenExchangeRatesClient exchangeRatesClient;
  private final DittoClient dittoClient;
  /**
   * Resource path to the config file.
   */
  private final String configPath;

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
  public void updateQuotes(int maxDays) throws IOException {
    if (maxDays <= 0) {
      return;
    }

    final OXRConfig config = loadConfig();
    // baseCurrencyCode is expected to be USD for our usage; OpenExchangeRates default base is USD.
    // For non-USD bases, we still proceed but log a warning when fetching.
    final Map<String, OXRCurrency> configByCode = new HashMap<>();
    for (OXRCurrency c : config.getQuoteCurrencies()) {
      configByCode.put(c.getCode(), c);
    }

    // Resolve USD currency ID for instrument quoteCurrencyId
    String txnId = null;
    final DittoResponse<List<Currency>> usdQueryResp = dittoClient.queryCurrencies(
      QueryCurrenciesRequest.builder()
        .transactionId(txnId)
        .ledgerId(null)
        .code(QUOTE_CURRENCY_CODE)
        .build());
    txnId = usdQueryResp.getTransactionId();
    if (usdQueryResp.getData().isEmpty()) {
      throw new IOException(String.format("Quote currency %s not found in Ditto store", QUOTE_CURRENCY_CODE));
    }
    if (usdQueryResp.getData().size() > 1) {
      throw new IOException(String.format("Multiple %s currencies found in Ditto store", QUOTE_CURRENCY_CODE));
    }
    final Currency usdCurrency = usdQueryResp.getData().get(0);
    final String usdCurrencyId = Objects.requireNonNull(usdCurrency.getId()).getId();

    // Load all existing global currencies
    final DittoResponse<List<Currency>> allCurResp = dittoClient.queryCurrencies(
      QueryCurrenciesRequest.builder()
        .transactionId(txnId)
        .ledgerId(null)
        .build());
    txnId = allCurResp.getTransactionId();
    final Map<String, Currency> dbCurrencyByCode = new HashMap<>();
    for (Currency c : allCurResp.getData()) {
      dbCurrencyByCode.put(c.getCode(), c);
    }

    // Prepare per-currency existing quote boundaries (UTC dates)
    final Map<String, LocalDate> earliestDateByCode = new HashMap<>();
    final Map<String, LocalDate> latestDateByCode = new HashMap<>();
    final Map<String, Instant> earliestInstantByCode = new HashMap<>();
    final Map<String, Instant> latestInstantByCode = new HashMap<>();
    final Map<String, String> instrumentIdByCode = new HashMap<>();

    for (OXRCurrency c : config.getQuoteCurrencies()) {
      final String code = c.getCode();
      if (QUOTE_CURRENCY_CODE.equalsIgnoreCase(code)) {
        continue; // skip quote currency
      }
      final Currency dbCur = dbCurrencyByCode.get(code);
      if (dbCur == null) {
        continue;
      }
      final String instrumentId = dbCur.getInstrumentId();
      instrumentIdByCode.put(code, instrumentId);

      // Query earliest quote
      DittoResponse<List<Quote>> earliestResp = dittoClient.queryQuotes(
        QueryQuotesRequest.builder()
          .transactionId(txnId)
          .instrumentId(instrumentId)
          .order(QuoteOrder.TIME_ASC)
          .limit(1)
          .build());
      txnId = earliestResp.getTransactionId();
      if (!earliestResp.getData().isEmpty()) {
        Instant t = earliestResp.getData().get(0).getTime();
        earliestInstantByCode.put(code, t);
        earliestDateByCode.put(code, t.atOffset(ZoneOffset.UTC).toLocalDate());
      }

      // Query latest quote
      DittoResponse<List<Quote>> latestResp = dittoClient.queryQuotes(
        QueryQuotesRequest.builder()
          .transactionId(txnId)
          .instrumentId(instrumentId)
          .order(QuoteOrder.TIME_DESC)
          .limit(1)
          .build());
      txnId = latestResp.getTransactionId();
      if (!latestResp.getData().isEmpty()) {
        Instant t = latestResp.getData().get(0).getTime();
        latestInstantByCode.put(code, t);
        latestDateByCode.put(code, t.atOffset(ZoneOffset.UTC).toLocalDate());
      }
    }

    // Compute target date list to fetch from OpenExchangeRates
    final LocalDate nowUtcDate = LocalDate.now(ZoneOffset.UTC);
    final LocalDate lastStableDate = nowUtcDate.minusDays(1); // only include days <= lastStableDate

    final List<LocalDate> dayPlan = new ArrayList<>();
    if (!latestDateByCode.isEmpty()) {
      // After-range: union of [min(latest + 1), lastStable]
      LocalDate forwardStart = null;
      for (LocalDate d : latestDateByCode.values()) {
        LocalDate start = d.plusDays(1);
        if (forwardStart == null || start.isBefore(forwardStart)) {
          forwardStart = start;
        }
      }
      if (forwardStart != null && !forwardStart.isAfter(lastStableDate)) {
        LocalDate d = forwardStart;
        while (!d.isAfter(lastStableDate) && dayPlan.size() < maxDays) {
          dayPlan.add(d);
          d = d.plusDays(1);
        }
      }

      // Before-range: union of [earliestAllowed, max(earliestDate)], processed from new to old
      if (dayPlan.size() < maxDays && !earliestDateByCode.isEmpty()) {
        LocalDate backwardStart = null; // this is the newest day to add on the left side
        for (LocalDate d : earliestDateByCode.values()) {
          if (backwardStart == null || d.isAfter(backwardStart)) {
            backwardStart = d;
          }
        }
        if (backwardStart != null) {
          LocalDate d = backwardStart; // inclusive
          while (!d.isBefore(EARLIEST_DATE) && dayPlan.size() < maxDays) {
            if (!dayPlan.contains(d)) {
              dayPlan.add(d);
            }
            d = d.minusDays(1);
          }
        }
      }
    } else {
      // All currencies have no quotes. Start from lastStableDate backwards.
      LocalDate d = lastStableDate;
      while (!d.isBefore(EARLIEST_DATE) && dayPlan.size() < maxDays) {
        dayPlan.add(d);
        d = d.minusDays(1);
      }
    }

    if (dayPlan.isEmpty()) {
      logger.info("No days to update (maxDays={}, lastStableDate={})", maxDays, lastStableDate);
      return;
    }

    // Process each day, fetch OXR once per day, then upsert quotes for all currencies
    for (LocalDate day : dayPlan) {
      HistoricalRatesResponse ratesResp = exchangeRatesClient.getHistoricalRates(
        HistoricalRatesRequest.builder()
          .date(day)
          .base(QUOTE_CURRENCY_CODE)
          .build());

      final Map<String, Double> rates = ratesResp.getRates();

      final Instant quoteTime = day.atStartOfDay().toInstant(ZoneOffset.UTC);

      for (OXRCurrency curConf : config.getQuoteCurrencies()) {
        final String code = curConf.getCode();
        if (QUOTE_CURRENCY_CODE.equalsIgnoreCase(code)) {
          continue;
        }
        final Double rate = rates.get(code);
        if (rate == null || rate.doubleValue() <= 0) {
          continue; // No data for this code on this date
        }

        Currency dbCur = dbCurrencyByCode.get(code);
        String instrumentId = (dbCur != null) ? dbCur.getInstrumentId() : null;

        // Decide whether this currency needs a quote for this day
        Instant dayInstant = quoteTime; // 00:00Z of the day
        Instant earliestInst = earliestInstantByCode.get(code);
        Instant latestInst = latestInstantByCode.get(code);
        boolean shouldInsert;
        if (earliestInst == null && latestInst == null) {
          shouldInsert = true;
        } else if (earliestInst != null && dayInstant.isBefore(earliestInst)) {
          shouldInsert = true;
        } else if (latestInst != null && dayInstant.isAfter(latestInst)) {
          shouldInsert = true;
        } else {
          shouldInsert = false;
        }
        if (!shouldInsert) {
          continue;
        }

        // Create currency/instrument if missing
        if (instrumentId == null) {
          // Determine CurrencyType and Instrument category
          CurrencyType currencyType = mapCurrencyType(curConf.getType());
          String symbol = String.format("%s/%s", code, QUOTE_CURRENCY_CODE);

          DittoResponse<CreateCurrencyResponse> createResp = dittoClient.createCurrency(
            CreateCurrencyRequest.builder()
              .transactionId(txnId)
              .ledgerId(null)
              .type(currencyType)
              .code(code)
              .name(curConf.getName())
              .symbol(symbol)
              .decimals(curConf.getDecimalPlaces())
              .category(mapInstrumentCategory(curConf.getType()))
              .quoteCurrencyId(usdCurrencyId)
              .initialQuote(BigDecimal.valueOf(rate))
              .build()
          );
          txnId = createResp.getTransactionId();
          // created currency id not used directly; instrumentId used below
          instrumentId = createResp.getData().getInstrumentId();

          // Update caches
          dbCur = Currency.builder()
            .id(null)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .archivedAt(null)
            .type(currencyType)
            .name(curConf.getName())
            .code(code)
            .decimals(curConf.getDecimalPlaces())
            .instrumentId(instrumentId)
            .build();
          dbCurrencyByCode.put(code, dbCur);
          instrumentIdByCode.put(code, instrumentId);
          // Set earliest/latest boundaries accordingly
          earliestDateByCode.put(code, day);
          latestDateByCode.put(code, day);
          earliestInstantByCode.put(code, dayInstant);
          latestInstantByCode.put(code, dayInstant);
        } else {
          // Existing instrument. Ensure initialQuote is updated if inserting before current earliest
          Instant earliestInst2 = earliestInstantByCode.get(code);
          if (earliestInst2 == null || dayInstant.isBefore(earliestInst2)) {
            final String symbol = String.format("%s/%s", code, QUOTE_CURRENCY_CODE);
            // Prepare update with new initialQuote
            DittoResponse<String> updateResp = dittoClient.updateInstrument(
              UpdateInstrumentRequest.builder()
                .transactionId(txnId)
                .ledgerId(null)
                .instrumentId(instrumentId)
                .symbol(symbol)
                .name(curConf.getName())
                .category(mapInstrumentCategory(curConf.getType()))
                .decimals(curConf.getDecimalPlaces())
                .quoteCurrencyId(usdCurrencyId)
                .initialQuote(BigDecimal.valueOf(rate))
                .build()
            );
            txnId = updateResp.getTransactionId();
            earliestDateByCode.put(code, day);
            earliestInstantByCode.put(code, dayInstant);
          }
        }

        // Create the quote
        DittoResponse<String> createQuoteResp = dittoClient.createQuote(
          CreateQuoteRequest.builder()
            .transactionId(txnId)
            .ledgerId(null)
            .instrumentId(instrumentId)
            .time(quoteTime)
            .value(BigDecimal.valueOf(rate))
            .source(SOURCE)
            .build()
        );
        txnId = createQuoteResp.getTransactionId();

        // Update boundaries
        LocalDate curEarliest = earliestDateByCode.get(code);
        if (curEarliest == null || day.isBefore(curEarliest)) {
          earliestDateByCode.put(code, day);
          earliestInstantByCode.put(code, dayInstant);
        }
        LocalDate curLatest = latestDateByCode.get(code);
        if (curLatest == null || day.isAfter(curLatest)) {
          latestDateByCode.put(code, day);
          latestInstantByCode.put(code, dayInstant);
        }
      }
    }
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

  private static InstrumentCategory mapInstrumentCategory(OXRCurrencyType type) {
    switch (type) {
      case FIAT:
        return InstrumentCategory.FOREX;
      case CRYPTO:
        return InstrumentCategory.CRYPTO;
      default:
        throw new IllegalArgumentException("Invalid currency type: " + type);
    }
  }
}
