package org.freedger.services.openexchangerates.models;

import java.util.Map;

/**
 * Response model for OpenExchangeRates historical rates API.
 * 
 * <p>Contains exchange rate data for a specific date, with all rates relative
 * to the base currency.</p>
 */
public class HistoricalRatesResponse {
    /**
     * Optional disclaimer text.
     */
    private String disclaimer;
    
    /**
     * Optional license information.
     */
    private String license;
    
    /**
     * UNIX timestamp indicating when the rates were published.
     * Note: In JavaScript, multiply by 1000 as it uses milliseconds.
     */
    private Long timestamp;
    
    /**
     * The base currency code (3-letter ISO currency code) to which all
     * exchange rates are relative (e.g., "USD").
     */
    private String base;
    
    /**
     * Map of currency codes to exchange rates.
     * Keys are 3-letter ISO currency codes, values are exchange rates
     * relative to 1 unit of the base currency.
     */
    private Map<String, Double> rates;
    
    /**
     * Gets the disclaimer text.
     * 
     * @return The disclaimer text, or null if not present
     */
    public String getDisclaimer() {
        return disclaimer;
    }
    
    /**
     * Sets the disclaimer text.
     * 
     * @param disclaimer The disclaimer text
     */
    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
    
    /**
     * Gets the license information.
     * 
     * @return The license information, or null if not present
     */
    public String getLicense() {
        return license;
    }
    
    /**
     * Sets the license information.
     * 
     * @param license The license information
     */
    public void setLicense(String license) {
        this.license = license;
    }
    
    /**
     * Gets the UNIX timestamp when the rates were published.
     * 
     * @return The UNIX timestamp in seconds
     */
    public Long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Sets the UNIX timestamp when the rates were published.
     * 
     * @param timestamp The UNIX timestamp in seconds
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Gets the base currency code.
     * 
     * @return The 3-letter ISO currency code (e.g., "USD")
     */
    public String getBase() {
        return base;
    }
    
    /**
     * Sets the base currency code.
     * 
     * @param base The 3-letter ISO currency code
     */
    public void setBase(String base) {
        this.base = base;
    }
    
    /**
     * Gets the exchange rates map.
     * 
     * @return Map of currency codes to exchange rates
     */
    public Map<String, Double> getRates() {
        return rates;
    }
    
    /**
     * Sets the exchange rates map.
     * 
     * @param rates Map of currency codes to exchange rates
     */
    public void setRates(Map<String, Double> rates) {
        this.rates = rates;
    }
    
    /**
     * Gets the exchange rate for a specific currency.
     * 
     * @param currencyCode The 3-letter ISO currency code
     * @return The exchange rate, or null if the currency is not in the response
     */
    public Double getRate(String currencyCode) {
        return rates != null ? rates.get(currencyCode) : null;
    }
    
    @Override
    public String toString() {
        return "HistoricalRatesResponse{" +
                "timestamp=" + timestamp +
                ", base='" + base + '\'' +
                ", ratesCount=" + (rates != null ? rates.size() : 0) +
                '}';
    }
}

