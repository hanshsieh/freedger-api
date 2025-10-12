# OpenExchangeRates HTTP Client

這是一個用於呼叫 OpenExchangeRates API 的 Java HTTP client，參考了 Ditto 的實作方式。

## 功能

目前支援以下功能：

- **歷史匯率查詢** (`/historical/*.json`): 獲取特定日期的歷史匯率資料（從 1999年1月1日 開始）

## 使用方式

### 基本使用

```java
import org.freedger.services.openexchangerates.OpenExchangeRatesHttpClient;
import org.freedger.services.openexchangerates.models.HistoricalRatesRequest;
import org.freedger.services.openexchangerates.models.HistoricalRatesResponse;
import java.time.LocalDate;

// 建立 client（使用預設的 API URL）
String appId = "YOUR_APP_ID";
OpenExchangeRatesHttpClient client = new OpenExchangeRatesHttpClient(appId);

// 使用 Builder pattern 建立請求
HistoricalRatesRequest request = HistoricalRatesRequest.builder()
    .date(LocalDate.of(2024, 1, 1))
    .build();

// 查詢歷史匯率
HistoricalRatesResponse response = client.getHistoricalRates(request);

// 取得匯率資訊
System.out.println("基準貨幣: " + response.getBase());
System.out.println("EUR 匯率: " + response.getRate("EUR"));
System.out.println("GBP 匯率: " + response.getRate("GBP"));
System.out.println("JPY 匯率: " + response.getRate("JPY"));

// 關閉 client
client.close();
```

### 使用變數建立請求

```java
// 使用變數
LocalDate date = LocalDate.of(2024, 1, 1);

HistoricalRatesRequest request = HistoricalRatesRequest.builder()
    .date(date)
    .build();

HistoricalRatesResponse response = client.getHistoricalRates(request);
```

### 進階使用（需要付費方案）

```java
// 指定基準貨幣和特定幣種
// 注意：此功能需要 Developer、Enterprise 或 Unlimited 方案
HistoricalRatesRequest request = HistoricalRatesRequest.builder()
    .date(LocalDate.of(2024, 1, 1))
    .base("EUR")                    // 基準貨幣
    .symbols("USD", "GBP", "JPY")   // 只取得這些幣種
    .build();

HistoricalRatesResponse response = client.getHistoricalRates(request);

System.out.println("基準貨幣: " + response.getBase()); // EUR
System.out.println("USD 匯率: " + response.getRate("USD"));
```

### 使用 List 指定幣種

```java
import java.util.List;

List<String> currencies = List.of("USD", "GBP", "JPY", "CNY", "TWD");

HistoricalRatesRequest request = HistoricalRatesRequest.builder()
    .date(LocalDate.of(2024, 1, 1))
    .base("EUR")
    .symbols(currencies)
    .build();

HistoricalRatesResponse response = client.getHistoricalRates(request);
```

### 自訂 API URL

```java
// 使用自訂的 API URL
String customUrl = "https://your-custom-url.com/api/";
OpenExchangeRatesHttpClient client = new OpenExchangeRatesHttpClient(customUrl, appId);
```

## Request 模型

`HistoricalRatesRequest` 使用 Builder pattern 建立，包含以下欄位：

- `date` (必填): 查詢日期（LocalDate）
- `base` (可選): 基準貨幣代碼（3 字母 ISO 代碼，例如 "EUR"）- 需要付費方案
- `symbols` (可選): 要查詢的貨幣代碼列表 - 需要付費方案

## Response 模型

`HistoricalRatesResponse` 包含以下欄位：

- `disclaimer`: 免責聲明（可選）
- `license`: 授權資訊（可選）
- `timestamp`: UNIX 時間戳（秒）
- `base`: 基準貨幣代碼（3 字母 ISO 代碼，例如 "USD"）
- `rates`: 匯率 Map，key 為貨幣代碼，value 為相對於基準貨幣的匯率

## 重要注意事項

1. **時區**: 所有日期和時間戳都使用 UTC 時區（GMT+00:00），不適用夏令時間
2. **歷史資料範圍**: 歷史匯率資料從 1999年1月1日 開始提供
3. **當日查詢**: 如果查詢當日 UTC 日期，會返回最新的可用匯率（與 `/latest.json` endpoint 相同）
4. **付費功能**: 更改基準貨幣（`base`）和指定特定幣種（`symbols`）需要 Developer、Enterprise 或 Unlimited 方案

## 錯誤處理

```java
try {
    HistoricalRatesRequest request = HistoricalRatesRequest.builder()
        .date(LocalDate.of(2024, 1, 1))
        .build();
    
    HistoricalRatesResponse response = client.getHistoricalRates(request);
    // 處理回應
} catch (IOException e) {
    // 處理錯誤（網路錯誤、API 錯誤等）
    System.err.println("無法取得匯率: " + e.getMessage());
}
```

## 實作細節

- 使用 Apache HttpClient 5 進行 HTTP 請求
- 使用 Gson 進行 JSON 序列化/反序列化
- 請求和回應逾時設定為 10 秒
- 支援連線池以提升效能
- 使用 SLF4J 記錄日誌

## API 文件

完整的 API 文件請參考：https://docs.openexchangerates.org/reference/historical-json

