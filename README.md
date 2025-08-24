# TradingView Screener Kt

[![GitHub Release](https://img.shields.io/github/v/release/enciyo/tradingviewscreenerkt)](https://github.com/enciyo/tradingviewscreenerkt/releases)
[![GitHub Packages](https://img.shields.io/badge/github%20packages-v1.0.0-blue.svg)](https://github.com/enciyo/tradingviewscreenerkt/packages)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.0-blue.svg?logo=kotlin)](http://kotlinlang.org)

A Kotlin library for creating custom stock screeners using TradingView's official API. This is a Kotlin port of the popular Python [tradingview-screener](https://pypi.org/project/tradingview-screener/) package.

## Overview

`tradingview-screener-kt` allows you to create powerful stock screeners using TradingView's official API directly from Kotlin/JVM applications. Unlike web scraping solutions, this library uses TradingView's legitimate API endpoints for reliable data access.

### Key Features

- **🎯 Stock Screening**: Query stocks with custom filters and conditions
- **🌍 Multiple Markets**: Support for US stocks and Turkish markets
- **🔍 Advanced Filtering**: SQL-like syntax with support for comparison operators
- **🚀 Type Safety**: Full Kotlin type safety and null safety
- **⚡ Coroutines Support**: Built with Kotlin coroutines for async operations
- **📦 JVM Integration**: Easy integration with any JVM-based application

## Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.enciyo:tradingviewscreenerkt:v1.0.1")
}
```

### Gradle (Groovy DSL)

```groovy
repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.enciyo:tradingviewscreenerkt:v1.0.1'
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.enciyo</groupId>
    <artifactId>tradingviewscreenerkt</artifactId>
    <version>v1.0.1</version>
</dependency>
```

### JitPack.io

This library is also available through [JitPack.io](https://jitpack.io/#enciyo/tradingviewscreenerkt):

[![JitPack](https://jitpack.io/v/enciyo/tradingviewscreenerkt.svg)](https://jitpack.io/#enciyo/tradingviewscreenerkt)

You can use any commit hash, branch name, or tag as the version:

```kotlin
// Latest commit from main branch
implementation("com.github.enciyo:tradingviewscreenerkt:main-SNAPSHOT")

// Specific commit hash
implementation("com.github.enciyo:tradingviewscreenerkt:abc1234")

// Specific tag
implementation("com.github.enciyo:tradingviewscreenerkt:v1.0.1")
```

## Quick Start

Here's a simple example to get you started:

```kotlin
import com.enciyo.*

suspend fun main() {
    val query = Query()
        .select(DefaultSelects.Name)
        .setMarkets(DefaultMarkets.America)
        .limit(100)
        .get()
    
    println("Query result: $query")
}
```

## Advanced Usage

### Basic Stock Screening

```kotlin
suspend fun basicScreener() {
    val query = Query()
        .select(DefaultSelects.Name)
        .setMarkets(DefaultMarkets.America)
        .limit(50)
        .get()
    
    println("Found stocks: $query")
}
```

### Turkish Market Screening

```kotlin
suspend fun turkishMarketScreener() {
    val query = Query()
        .select(DefaultSelects.Name)
        .setMarkets(DefaultMarkets.Turkey)
        .limit(1000)
        .get()
    
    println("Turkish stocks: $query")
}
```

## Available Fields

Currently supported fields:

```kotlin
enum class DefaultSelects(
    override val value: String
) : Select {
    Name("name"),          // Stock name/ticker
    
    @Deprecated("Use Name instead")
    Ticker("ticker");      // Legacy ticker field
}
```

### Creating Custom Fields

Users can create their own field definitions by implementing the `Select` interface:

```kotlin
// Custom field enum
enum class CustomSelects(
    override val value: String
) : Select {
    Name("name"),
    Close("close"),
    Volume("volume"),
    MarketCap("market_cap_basic"),
    PE("price_earnings_ttm"),
    RSI("RSI"),
    MACD("MACD.macd")
}

// Single-use custom field
val customField = object : Select {
    override val value: String = "custom_field_name"
}

// Usage
Query()
    .select(CustomSelects.Close, CustomSelects.Volume)
    .setMarkets(DefaultMarkets.America)
    .get()
```

## Available Markets

Currently supported markets:

```kotlin
enum class DefaultMarkets(
    override val value: String
) : Market {
    America("america"),    // US Stocks
    Turkey("turkey");      // Turkish Markets
}
```

### Creating Custom Markets

Users can create their own market definitions by implementing the `Market` interface:

```kotlin
// Custom market enum
enum class CustomMarkets(
    override val value: String
) : Market {
    America("america"),
    Turkey("turkey"),
    Crypto("crypto"),
    Forex("forex"),
    Futures("futures")
}

// Single-use custom market
val customMarket = object : Market {
    override val value: String = "custom_market"
}

// Usage
Query()
    .select(DefaultSelects.Name)
    .setMarkets(CustomMarkets.Crypto)
    .get()
```

## Query Operations

### Basic Query Structure

```kotlin
val query = Query()
    .select(DefaultSelects.Name)           // Select fields
    .setMarkets(DefaultMarkets.America)    // Set market
    .limit(100)                            // Set result limit
    .get()                                 // Execute query
```

### Market Selection

```kotlin
// US Market
.setMarkets(DefaultMarkets.America)

// Turkish Market  
.setMarkets(DefaultMarkets.Turkey)

// Custom market
.setMarkets(market("custom_market_name"))
```

## Error Handling

```kotlin
suspend fun safeScreening() {
    try {
        val result = Query()
            .select(DefaultSelects.Name)
            .setMarkets(DefaultMarkets.America)
            .limit(100)
            .get()
        
        println("Successfully retrieved data: $result")
    } catch (e: Exception) {
        println("Error occurred: ${e.message}")
    }
}
```

## Performance Tips

1. **Limit your queries**: Use `.limit()` to avoid overwhelming the API
2. **Use specific fields**: Only select the fields you need
3. **Respect rate limits**: Add delays between successive API calls

```kotlin
suspend fun efficientScreening() {
    val query = Query()
        .select(DefaultSelects.Name)  // Only necessary fields
        .setMarkets(DefaultMarkets.America)
        .limit(50)  // Reasonable limit
        .get()
    
    // delay(1000)  // Rate limiting // This line was removed from the original file
}
```

## Development Status

⚠️ **Note**: This library is currently in active development. Some features mentioned in the original Python version are being implemented:

- [ ] Extended field support (3000+ fields)
- [ ] Advanced filtering operations
- [ ] Real-time data access
- [ ] Additional markets (Crypto, Forex, Futures)
- [ ] Technical indicators support

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