# TradingView Screener Kt

[![GitHub Release](https://img.shields.io/github/v/release/enciyo/tradingviewscreenerkt)](https://github.com/enciyo/tradingviewscreenerkt/releases)
[![GitHub Packages](https://img.shields.io/badge/github%20packages-v1.0.0-blue.svg)](https://github.com/enciyo/tradingviewscreenerkt/packages)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.0-blue.svg?logo=kotlin)](http://kotlinlang.org)


A Kotlin library for creating custom stock screeners using TradingView's official API. This is a Kotlin port of the popular Python [tradingview-screener](https://pypi.org/project/tradingview-screener/) package.

## Overview

`tradingview-screener-kt` allows you to create powerful stock screeners using TradingView's official API directly from Kotlin/JVM applications. Unlike web scraping solutions, this library uses TradingView's legitimate API endpoints for reliable data access.

### Key Features

- **🎯 Over 3000 Fields**: OHLC data, technical indicators, fundamental metrics, and much more
- **🌍 Multiple Markets**: Stocks, crypto, forex, CFD, futures, bonds, and more
- **⏱️ Customizable Timeframes**: 1 minute, 5 minutes, 1 hour, 1 day, and more for each field
- **🔍 Advanced Filtering**: SQL-like syntax with support for And/Or operators
- **📊 Real-time Data**: Access to live market data (with authentication)
- **🚀 Type Safety**: Full Kotlin type safety and null safety
- **⚡ Coroutines Support**: Built with Kotlin coroutines for async operations

## Installation

### Gradle (Kotlin DSL)

```
repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven {
        url = uri("https://maven.pkg.github.com/enciyo/tradingviewscreenerkt")
    }
}
```

```kotlin
dependencies {
    implementation("com.enciyo:tradingviewscrennerkt:1.0.0")
}
```

### Gradle (Groovy DSL)

```groovy
dependencies {
    implementation 'com.enciyo:tradingviewscrennerkt:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>com.enciyo</groupId>
    <artifactId>tradingviewscrennerkt</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

Here's a simple example to get you started:

```kotlin
import com.enciyo.tradingviewscrennerkt.*

suspend fun main() {
    val query = Query()
        .select("name", "close", "volume", "market_cap_basic")
        .getScannerData()
    
    println("Found ${query.totalCount} stocks")
    query.data.forEach { stock ->
        println("${stock.name}: $${stock.close} (Volume: ${stock.volume})")
    }
}
```

**Output:**
```
Found 17580 stocks
NVDA: $127.25 (Volume: 298220762)
SPY: $558.70 (Volume: 33701795)
TSLA: $221.10 (Volume: 73869589)
...
```

## Advanced Usage

### Complex Filtering with Multiple Conditions

```kotlin
suspend fun advancedScreener() {
    val query = Query()
        .select("name", "close", "volume", "relative_volume_10d_calc", "market_cap_basic")
        .where(
            Column("market_cap_basic").between(1_000_000, 50_000_000),
            Column("relative_volume_10d_calc") > 1.2,
            Column("MACD.macd") >= Column("MACD.signal")
        )
        .orderBy("volume", ascending = false)
        .offset(5)
        .limit(25)
        .getScannerData()
    
    query.data.forEach { stock ->
        println("${stock.name}: Volume ${stock.volume}, Market Cap: ${stock.marketCapBasic}")
    }
}
```

### Using Different Markets and Timeframes

```kotlin
suspend fun cryptoScreener() {
    val query = Query()
        .market(Market.CRYPTO)
        .select("name", "close", "volume", "change")
        .where(
            Column("volume") > 1_000_000,
            Column("change") > 5.0
        )
        .orderBy("change", ascending = false)
        .limit(20)
        .getScannerData()
    
    println("Top gaining cryptocurrencies:")
    query.data.forEach { crypto ->
        println("${crypto.name}: ${crypto.change}% change")
    }
}
```

### Technical Analysis Screening

```kotlin
suspend fun technicalScreener() {
    val query = Query()
        .select("name", "close", "RSI", "MACD.macd", "MACD.signal", "EMA50", "SMA200")
        .where(
            Column("RSI") < 30,  // Oversold
            Column("MACD.macd") > Column("MACD.signal"),  // MACD bullish crossover
            Column("close") > Column("EMA50"),  // Above 50-day EMA
            Column("volume") > Column("average_volume_30d_calc") * 1.5  // High volume
        )
        .orderBy("RSI", ascending = true)
        .limit(15)
        .getScannerData()
    
    println("Potentially oversold stocks with bullish signals:")
    query.data.forEach { stock ->
        println("${stock.name}: RSI ${stock.rsi}, Price $${stock.close}")
    }
}
```

## Real-Time Data Access

To access real-time data, you need to provide authentication cookies:

```kotlin
suspend fun realTimeData() {
    val cookies = mapOf("sessionid" to "your_session_id_here")
    
    val query = Query()
        .select("name", "close", "volume", "update_mode")
        .cookies(cookies)
        .getScannerData()
    
    query.data.forEach { stock ->
        println("${stock.name}: $${stock.close} (${stock.updateMode})")
    }
}
```

### Getting Session Cookies

1. **Manual extraction:**
   - Go to [TradingView](https://www.tradingview.com)
   - Open Developer Tools (F12)
   - Navigate to Application → Cookies → https://www.tradingview.com/
   - Copy the `sessionid` value

2. **Programmatic login** (use cautiously due to rate limits):

```kotlin
import io.ktor.client.*
import io.ktor.client.request.*

suspend fun authenticate(username: String, password: String): Map<String, String> {
    val client = HttpClient()
    // Implementation for authentication
    // Note: This may trigger CAPTCHA and account restrictions
}
```

## Comparison to Python Version

This Kotlin library provides the same powerful features as the original [Python tradingview-screener](https://pypi.org/project/tradingview-screener/) with additional benefits:

| Feature | Python Version | Kotlin Version |
|---------|---------------|----------------|
| 3000+ Fields | ✅ | ✅ |
| Multiple Markets | ✅ | ✅ |
| Real-time Data | ✅ | ✅ |
| Type Safety | ❌ | ✅ |
| Null Safety | ❌ | ✅ |
| Coroutines Support | ❌ | ✅ |
| JVM Integration | ❌ | ✅ |
| Android Support | ❌ | ✅ |
| GitHub Packages | ❌ | ✅ |

## Available Fields

The library supports over 3000 fields including:

### Basic Data
- `name`, `description`, `close`, `open`, `high`, `low`
- `volume`, `market_cap_basic`, `price_earnings_ttm`
- `dividend_yield_recent`, `earnings_per_share_basic_ttm`

### Technical Indicators
- `RSI`, `MACD.macd`, `MACD.signal`, `MACD.hist`
- `EMA5`, `EMA10`, `EMA20`, `EMA50`, `EMA200`
- `SMA5`, `SMA10`, `SMA20`, `SMA50`, `SMA200`
- `BB.upper`, `BB.lower`, `Stoch.K`, `Stoch.D`

### Fundamental Data
- `total_revenue`, `net_income`, `total_debt`
- `price_book_ratio`, `return_on_equity`, `debt_to_equity`
- `operating_margin`, `profit_margin`

For a complete list of available fields, visit the [documentation](https://github.com/enciyo/tradingviewscreenerkt/wiki/Fields).

## Markets

Supported markets include:

```kotlin
enum class Market {
    AMERICA,    // US Stocks
    CRYPTO,     // Cryptocurrencies  
    FOREX,      // Foreign Exchange
    CFD,        // Contracts for Difference
    FUTURES,    // Futures Contracts
    BONDS,      // Bonds
    EURONEXT,   // European Markets
    // ... and more
}
```

## Query Operations

### Filtering Operations

```kotlin
// Comparison operators
Column("price") > 100
Column("price") >= 100
Column("price") < 50
Column("price") <= 50
Column("price") eq 75

// Range operations
Column("market_cap").between(1_000_000, 100_000_000)
Column("sector").isIn("Technology", "Healthcare", "Finance")

// Pattern matching
Column("name").contains("Apple")
Column("description").startsWith("Technology")

// Logical operations
And(
    Column("volume") > 1_000_000,
    Column("price") < 50
)

Or(
    Column("sector") eq "Technology",
    Column("sector") eq "Healthcare"
)
```

## Error Handling

```kotlin
suspend fun safeScreening() {
    try {
        val result = Query()
            .select("name", "close", "volume")
            .where(Column("volume") > 1_000_000)
            .getScannerData()
        
        println("Successfully retrieved ${result.totalCount} stocks")
    } catch (e: TradingViewException) {
        println("TradingView API error: ${e.message}")
    } catch (e: NetworkException) {
        println("Network error: ${e.message}")
    }
}
```

## Performance Tips

1. **Limit your queries**: Use `.limit()` to avoid overwhelming the API
2. **Use specific fields**: Only select the fields you need
3. **Implement caching**: Cache results for repeated queries
4. **Respect rate limits**: Add delays between successive API calls

```kotlin
suspend fun efficientScreening() {
    val query = Query()
        .select("name", "close", "volume")  // Only necessary fields
        .where(Column("volume") > 500_000)
        .limit(50)  // Reasonable limit
        .getScannerData()
    
    delay(1000)  // Rate limiting
}
```

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

## Acknowledgments

This library is a Kotlin port of the excellent Python [tradingview-screener](https://pypi.org/project/tradingview-screener/) package by [shner-elmo](https://github.com/shner-elmo). We thank the original author for creating such a comprehensive and well-documented library.



## Support

- 📚 [Documentation](https://github.com/enciyo/tradingviewscreenerkt/wiki)
- 🐛 [Issue Tracker](https://github.com/enciyo/tradingviewscreenerkt/issues)
- 💬 [Discussions](https://github.com/enciyo/tradingviewscreenerkt/discussions)

## Star History

If this library has brought value to your projects, please consider giving it a star! ⭐

---

**Disclaimer**: This library is not affiliated with TradingView. Use it responsibly and in accordance with TradingView's terms of service.
