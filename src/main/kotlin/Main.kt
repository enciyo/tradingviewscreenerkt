package com.enciyo

fun main() {

    val marketQuery = Query()
        .select(DefaultSelects.Name)
        .setMarkets(DefaultMarkets.Turkey)
        .limit(1000)
        .get()
        .let {
            println(it)
        }

}

/* 
Kullanıcılar kendi metriklerini yazabilir!
Sadece Select ve Market interface'lerini override etmeleri yeterli.

Örnek 1: Kendi Select alanları
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

Örnek 2: Kendi Market tanımları  
enum class CustomMarkets(
    override val value: String
) : Market {
    America("america"),
    Turkey("turkey"),
    Crypto("crypto"),
    Forex("forex"),
    Futures("futures")
}

Örnek 3: Tek seferlik kullanım
val customField = object : Select {
    override val value: String = "custom_field_name"
}

val customMarket = object : Market {
    override val value: String = "custom_market"
}

Kullanım:
Query()
    .select(CustomSelects.Close, CustomSelects.Volume)
    .setMarkets(CustomMarkets.Crypto)
    .limit(100)
    .get()
*/