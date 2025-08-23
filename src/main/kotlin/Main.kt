package com.enciyo

fun main() {
    println("TradingView Screener Query Test")
    println("================================")

    val marketQuery = Query()
        .where(
            col("name").like("BIMAS")
        )
        .select("name")
        .setMarkets("turkey")
        .getScannerDataRaw()


    println(marketQuery)


}