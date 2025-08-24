package com.enciyo

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.json.*
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * This class allows you to perform SQL-like queries on the tradingview stock-screener.
 *
 * The `Query` object represents a query that can be made to the official tradingview API, and it
 * stores all the data as JSON internally.
 *
 * Examples:
 *
 * To perform a simple query all you have to do is:
 * ```kotlin
 * Query().getScannerData()
 * ```
 *
 * The `getScannerData()` method will return a tuple with the first element being the number of
 * records that were found (like a `COUNT(*)`), and the second element contains the data that was
 * found as a List.
 *
 * By default, the `Query` will select the columns: `name`, `close`, `volume`, `market_cap_basic`,
 * but you can override that:
 * ```kotlin
 * Query()
 *     .select("open", "high", "low", "VWAP", "MACD.macd", "RSI", "Price to Earnings Ratio (TTM)")
 *     .getScannerData()
 * ```
 *
 * You can find the 250+ columns available in `tradingview_screener.constants.COLUMNS`.
 *
 * Now let's do some queries using the `WHERE` statement, select all the stocks that the `close` is
 * bigger or equal than 350:
 * ```kotlin
 * Query()
 *     .select("close", "volume", "52 Week High")
 *     .where(Column("close").greaterThanOrEqual(350))
 *     .getScannerData()
 * ```
 *
 * You can even use other columns in these kind of operations:
 * ```kotlin
 * Query()
 *     .select("close", "VWAP")
 *     .where(Column("close").greaterThanOrEqual(Column("VWAP")))
 *     .getScannerData()
 * ```
 *
 * Let's find all the stocks that the price is between the EMA 5 and 20, and the type is a stock or fund:
 * ```kotlin
 * Query()
 *     .select("close", "volume", "EMA5", "EMA20", "type")
 *     .where(
 *         Column("close").between(Column("EMA5"), Column("EMA20")),
 *         Column("type").isin(listOf("stock", "fund"))
 *     )
 *     .getScannerData()
 * ```
 *
 * There are also the `ORDER BY`, `OFFSET`, and `LIMIT` statements.
 * Let's select all the tickers with a market cap between 1M and 50M, that have a relative volume
 * bigger than 1.2, and that the MACD is positive:
 * ```kotlin
 * Query()
 *     .select("name", "close", "volume", "relative_volume_10d_calc")
 *     .where(
 *         Column("market_cap_basic").between(1_000_000, 50_000_000),
 *         Column("relative_volume_10d_calc").greaterThan(1.2),
 *         Column("MACD.macd").greaterThanOrEqual(Column("MACD.signal"))
 *     )
 *     .orderBy("volume", ascending = false)
 *     .offset(5)
 *     .limit(15)
 *     .getScannerData()
 * ```
 *
 * To avoid rewriting the same query again and again, you can save the query to a variable and
 * just call `getScannerData()` again and again to get the latest data:
 * ```kotlin
 * val top50Bullish = Query()
 *     .select("name", "close", "volume", "relative_volume_10d_calc")
 *     .where(
 *         Column("market_cap_basic").between(1_000_000, 50_000_000),
 *         Column("relative_volume_10d_calc").greaterThan(1.2),
 *         Column("MACD.macd").greaterThanOrEqual(Column("MACD.signal"))
 *     )
 *     .orderBy("volume", ascending = false)
 *     .limit(50)
 *
 * top50Bullish.getScannerData()
 * ```
 */
class Query {

    companion object {
        private val DEFAULT_RANGE = listOf(0, 50)
        private const val URL = "https://scanner.tradingview.com/{market}/scan"
        private val HEADERS = mapOf(
            "authority" to "scanner.tradingview.com",
            "sec-ch-ua" to "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"98\", \"Google Chrome\";v=\"98\"",
            "accept" to "text/plain, */*; q=0.01",
            "content-type" to "application/json; charset=UTF-8",
            "sec-ch-ua-mobile" to "?0",
            "user-agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36",
            "sec-ch-ua-platform" to "\"Windows\"",
            "origin" to "https://www.tradingview.com",
            "sec-fetch-site" to "same-site",
            "sec-fetch-mode" to "cors",
            "sec-fetch-dest" to "empty",
            "referer" to "https://www.tradingview.com/",
            "accept-language" to "en-US,en;q=0.9,it;q=0.8"
        )

        // HTTP Client instance
        private val httpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private var query: QueryDict
    private var url: String

    init {
        query = QueryDict(
            markets = DefaultMarkets.entries,
            symbols = SymbolsDict(
                query = mapOf("types" to emptyList()),
                tickers = emptyList()
            ),
            options = mapOf("lang" to "en"),
            columns = DefaultSelects.entries,
            sort = SortByDict("Value.Traded", "desc"),
            range = DEFAULT_RANGE.toMutableList()
        )
        url = "https://scanner.tradingview.com/america/scan"
    }

    /**
     * Select specific columns for the query
     */
    fun select(vararg columns: Select): Query {
        query = query.copy(columns = columns.toList())
        return this
    }

    /**
     * Filter screener (expressions are joined with the AND operator)
     */
    fun where(vararg expressions: FilterOperationDict): Query {
        query = query.copy(filter = expressions.toList())
        return this
    }

    /**
     * Filter screener using AND/OR operators (nested expressions also allowed)
     *
     * Rules:
     * 1. The argument passed to `where2()` **must** be wrapped in `And()` or `Or()`.
     * 2. `And()` and `Or()` can accept one or more conditions as arguments.
     * 3. Conditions can be simple (e.g., `Column('field').equalTo('value')`) or complex, allowing nesting of `And()` and `Or()` to create intricate logical filters.
     * 4. Unlike the `where()` method, which only supports chaining conditions with the `AND` operator, `where2()` allows mixing and nesting of `AND` and `OR` operators.
     *
     * Examples:
     *
     * 1. **Combining conditions with `OR` and nested `AND`:**
     * ```kotlin
     * Query()
     *     .select("type", "typespecs")
     *     .where2(
     *         Or(
     *             And(Column("type").equalTo("stock"), Column("typespecs").has(listOf("common", "preferred"))),
     *             And(Column("type").equalTo("fund"), Column("typespecs").hasNoneOf(listOf("etf"))),
     *             Column("type").equalTo("dr")
     *         )
     *     )
     * ```
     *
     * This query filters entities where:
     * - The `type` is `'stock'` and `typespecs` contains `'common'` or `'preferred'`, **OR**
     * - The `type` is `'fund'` and `typespecs` does not contain `'etf'`, **OR**
     * - The `type` is `'dr'`.
     *
     * 2. **Mixing conditions with `OR`:**
     * ```kotlin
     * Query().where2(
     *     Or(
     *         And(col("type").equalTo("stock"), col("typespecs").has(listOf("common"))),
     *         col("type").equalTo("fund")
     *     )
     * )
     * ```
     * This query filters entities where:
     * - The `type` is `'stock'` and `typespecs` contains `'common'`, **OR**
     * - The `type` is `'fund'`.
     *
     * 3. **Combining conditions with `AND`:**
     * ```kotlin
     * Query()
     *     .setMarkets("crypto")
     *     .where2(
     *         And(
     *             col("exchange").isin(listOf("UNISWAP3POLYGON", "VERSEETH", "a", "fffffffff")),
     *             col("currency_id").equalTo("USD")
     *         )
     *     )
     * ```
     * This query filters entities in the `'crypto'` market where:
     * - The `exchange` is one of `'UNISWAP3POLYGON', 'VERSEETH', 'a', 'fffffffff'`, **AND**
     * - The `currency_id` is `'USD'`.
     */
    fun where2(operation: OperationDict): Query {
        query = query.copy(filter2 = operation.operation)
        return this
    }

    /**
     * Applies sorting to the query results based on the specified column.
     *
     * Examples:
     *
     * ```kotlin
     * Query().orderBy("volume", ascending = false) // sort descending
     * Query().orderBy("close", ascending = true)
     * Query().orderBy("dividends_yield_current", ascending = false, nullsFirst = false)
     * ```
     *
     * @param column Either a `Column` object or a string with the column name.
     * @param ascending Set to true for ascending order (default), or false for descending.
     * @param nullsFirst If true, places `null` values at the beginning of the results. Defaults to false.
     * @return The updated query object.
     */
    fun orderBy(column: String, ascending: Boolean = true, nullsFirst: Boolean = false): Query {
        val sortDict = SortByDict(
            sortBy = column,
            sortOrder = if (ascending) "asc" else "desc",
            nullsFirst = nullsFirst
        )
        query = query.copy(sort = sortDict)
        return this
    }

    fun orderBy(column: Select, ascending: Boolean = true, nullsFirst: Boolean = false): Query {
        return orderBy(column.value, ascending, nullsFirst)
    }

    /**
     * Set the limit for the number of results returned
     */
    fun limit(limit: Int): Query {
        val currentRange = query.range?.toMutableList() ?: DEFAULT_RANGE.toMutableList()
        currentRange[1] = limit
        query = query.copy(range = currentRange)
        return this
    }

    /**
     * Set the offset for pagination
     */
    fun offset(offset: Int): Query {
        val currentRange = query.range?.toMutableList() ?: DEFAULT_RANGE.toMutableList()
        currentRange[0] = offset
        query = query.copy(range = currentRange)
        return this
    }

    /**
     * This method allows you to select the market/s which you want to query.
     *
     * By default, the screener will only scan US equities, but you can change it to scan any
     * market or country, that includes a list of 67 countries, and also the following
     * asset classes: `bonds`, `cfd`, `coin`, `crypto`, `euronext`, `forex`,
     * `futures`, `options`.
     *
     * Examples:
     *
     * By default, the screener will show results from the `america` market, but you can
     * change it (note the difference between `market` and `country`)
     * ```kotlin
     * val columns = listOf("close", "market", "country", "currency")
     * Query()
     *     .select(*columns.toTypedArray())
     *     .setMarkets("italy")
     *     .getScannerData()
     * ```
     *
     * You can also select multiple markets
     * ```kotlin
     * Query()
     *     .select(*columns.toTypedArray())
     *     .setMarkets("america", "israel", "hongkong", "switzerland")
     *     .getScannerData()
     * ```
     *
     * You may also select different financial instruments
     * ```kotlin
     * Query()
     *     .select("close", "market")
     *     .setMarkets("cfd", "crypto", "forex", "futures")
     *     .getScannerData()
     * ```
     *
     * @param markets one or more markets
     * @return Self
     */
    fun setMarkets(vararg markets: Market): Query {
        when (markets.size) {
            1 -> {
                val market = markets[0]
                url = URL.replace("{market}", market.value)
                query = query.copy(markets = listOf(market))
            }

            else -> { // 0 or > 1
                url = URL.replace("{market}", "global")
                query = query.copy(markets = markets.toList())
            }
        }
        return this
    }

    /**
     * Set the tickers you wish to receive information on.
     *
     * Note that this resets the markets and sets the URL market to `global`.
     *
     * Examples:
     *
     * ```kotlin
     * val q = Query().select("name", "market", "close", "volume", "VWAP", "MACD.macd")
     * q.setTickers("NASDAQ:TSLA").getScannerData()
     * ```
     *
     * To set tickers from multiple markets we need to update the markets that include them:
     * ```kotlin
     * Query()
     *     .setMarkets("america", "italy", "vietnam")
     *     .setTickers("NYSE:GME", "AMEX:SPY", "MIL:RACE", "HOSE:VIX")
     *     .getScannerData()
     * ```
     *
     * @param tickers One or more tickers, syntax: `exchange:symbol`
     * @return Self
     */
    fun setTickers(vararg tickers: String): Query {
        val currentSymbols = query.symbols ?: SymbolsDict()
        query = query.copy(symbols = currentSymbols.copy(tickers = tickers.toList()))
        setMarkets() // reset to global
        return this
    }

    /**
     * Scan only the equities that are in the given index (or indexes).
     *
     * Note that this resets the markets and sets the URL market to `global`.
     *
     * Examples:
     *
     * ```kotlin
     * Query().setIndex("SYML:SP;SPX").getScannerData()
     * ```
     *
     * You can set multiple indices as well, like the NIFTY 50 and UK 100 Index.
     * ```kotlin
     * Query().setIndex("SYML:NSE;NIFTY", "SYML:TVC;UKX").getScannerData()
     * ```
     *
     * You can find the full list of indices in `constants.INDICES`,
     * just note that the syntax is `SYML:{source};{symbol}`.
     *
     * @param indexes One or more strings representing the financial indexes to filter by
     * @return An instance of the `Query` class with the filter applied
     */
    fun setIndex(vararg indexes: String): Query {
        query = query.copy(preset = "index_components_market_pages")
        val currentSymbols = query.symbols ?: SymbolsDict()
        query = query.copy(symbols = currentSymbols.copy(symbolset = indexes.toList()))
        // reset markets list and URL to `/global`
        setMarkets()
        return this
    }

    /**
     * Set a custom property on the query
     */
    fun setProperty(key: String, value: Any): Query {
        // Note: This would require a more dynamic approach in Kotlin
        // For now, we'll implement specific setters for known properties
        when (key) {
            "markets" -> if (value is List<*>) query = query.copy(markets = value as List<Market>)
            "columns" -> if (value is List<*>) query = query.copy(columns = value as List<Select>)
            "range" -> if (value is List<*>) query = query.copy(range = value as List<Int>)
            "preset" -> if (value is String) query = query.copy(preset = value)
            "ignoreUnknownFields" -> if (value is Boolean) query = query.copy(ignoreUnknownFields = value)
            // Add more properties as needed
        }
        return this
    }

    /**
     * Perform a request and return the raw data from the API (ScreenerDict).
     *
     * This function makes a real HTTP POST request to the TradingView API.
     */
    private fun getScannerDataRaw(): ScreenerDict {
        try {
            // Ensure range is set
            query = query.copy(range = query.range ?: DEFAULT_RANGE.toMutableList())

            // Convert query to JSON
            val jsonBody = buildJsonObject {
                put("markets", buildJsonArray { query.markets?.map { it.value }?.forEach { add(it) } })
                put("symbols", buildJsonObject {
                    query.symbols?.let { symbols ->
                        symbols.query?.let { query ->
                            put("query", buildJsonObject {
                                query.forEach { (key, value) ->
                                    put(key, buildJsonArray { value.forEach { add(it) } })
                                }
                            })
                        }
                        symbols.tickers?.let { tickers ->
                            put("tickers", buildJsonArray { tickers.forEach { add(it) } })
                        }
                        symbols.symbolset?.let { symbolset ->
                            put("symbolset", buildJsonArray { symbolset.forEach { add(it) } })
                        }
                        symbols.watchlist?.let { watchlist ->
                            put("watchlist", buildJsonObject {
                                watchlist.forEach { (key, value) ->
                                    put(key, value)
                                }
                            })
                        }
                        symbols.groups?.let { groups ->
                            put("groups", buildJsonArray {
                                groups.forEach { group ->
                                    add(buildJsonObject {
                                        group.forEach { (key, value) ->
                                            put(key, value)
                                        }
                                    })
                                }
                            })
                        }
                    }
                })
                put("options", buildJsonObject {
                    query.options?.forEach { (key, value) ->
                        when (value) {
                            is String -> put(key, value)
                            is Number -> put(key, value)
                            is Boolean -> put(key, value)
                            else -> put(key, value.toString())
                        }
                    }
                })
                put("columns", buildJsonArray { query.columns?.map { it.value }?.forEach { add(it) } })
                query.filter?.let { filter ->
                    put("filter", buildJsonArray {
                        filter.forEach { filterOp ->
                            add(buildJsonObject {
                                put("left", filterOp.left)
                                put("operation", filterOp.operation)
                                when (val right = filterOp.right) {
                                    is String -> put("right", right)
                                    is Number -> put("right", right)
                                    is Boolean -> put("right", right)
                                    is List<*> -> put("right", buildJsonArray { right.forEach { add(it.toString()) } })
                                    else -> put("right", right?.toString() ?: "")
                                }
                            })
                        }
                    })
                }
                query.filter2?.let { filter2 ->
                    put("filter2", buildJsonObject {
                        put("operator", filter2.operator)
                        put("operands", buildJsonArray {
                            filter2.operands.forEach { operand ->
                                when (operand) {
                                    is ExpressionDict -> add(buildJsonObject {
                                        put("expression", buildJsonObject {
                                            put("left", operand.expression.left)
                                            put("operation", operand.expression.operation)
                                            when (val right = operand.expression.right) {
                                                is String -> put("right", right)
                                                is Number -> put("right", right)
                                                is Boolean -> put("right", right)
                                                is List<*> -> put(
                                                    "right",
                                                    buildJsonArray { right.forEach { add(it.toString()) } })

                                                else -> put("right", right?.toString() ?: "")
                                            }
                                        })
                                    })

                                    is OperationDict -> add(buildJsonObject {
                                        put("operation", buildJsonObject {
                                            put("operator", operand.operation.operator)
                                            put("operands", buildJsonArray {
                                                operand.operation.operands.forEach { op ->
                                                    when (op) {
                                                        is ExpressionDict -> add(buildJsonObject {
                                                            put("expression", buildJsonObject {
                                                                put("left", op.expression.left)
                                                                put("operation", op.expression.operation)
                                                                when (val right = op.expression.right) {
                                                                    is String -> put("right", right)
                                                                    is Number -> put("right", right)
                                                                    is Boolean -> put("right", right)
                                                                    is List<*> -> put(
                                                                        "right",
                                                                        buildJsonArray { right.forEach { add(it.toString()) } })

                                                                    else -> put("right", right?.toString() ?: "")
                                                                }
                                                            })
                                                        })

                                                        else -> add(op.toString())
                                                    }
                                                }
                                            })
                                        })
                                    })

                                    else -> add(operand.toString())
                                }
                            }
                        })
                    })
                }
                query.sort?.let { sort ->
                    put("sort", buildJsonObject {
                        put("sortBy", sort.sortBy)
                        put("sortOrder", sort.sortOrder)
                        sort.nullsFirst?.let { put("nullsFirst", it) }
                    })
                }
                put("range", buildJsonArray { query.range?.forEach { add(it) } })
                put("ignoreUnknownFields", query.ignoreUnknownFields)
                query.preset?.let { put("preset", it) }
                query.priceConversion?.let { priceConv ->
                    put("price_conversion", buildJsonObject {
                        priceConv.forEach { (key, value) ->
                            when (value) {
                                is String -> put(key, value)
                                is Boolean -> put(key, value)
                                else -> put(key, value.toString())
                            }
                        }
                    })
                }
            }

            // Create request
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .apply {
                    HEADERS.forEach { (key, value) ->
                        addHeader(key, value)
                    }
                }
                .build()

            // Execute request
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }

            val responseBody = response.body?.string()
                ?: throw IOException("Empty response body")

            // Parse response
            val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject

            val totalCount = jsonResponse["totalCount"]?.jsonPrimitive?.int ?: 0
            val dataArray = jsonResponse["data"]?.jsonArray ?: buildJsonArray { }

            val data = dataArray.map { rowElement ->
                val row = rowElement.jsonObject
                val symbol = row["s"]?.jsonPrimitive?.content ?: ""
                val dataList = row["d"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
                ScreenerRowDict(symbol, dataList)
            }

            return ScreenerDict(totalCount, data)

        } catch (e: Exception) {
            println("Error making API request: ${e.message}")
            e.printStackTrace()
            // Return empty result on error
            return ScreenerDict(0, emptyList())
        }
    }

    /**
     * Perform a request and return the data from the API as a list along with
     * the number of rows/tickers that matched your query.
     *
     * @return a pair consisting of: (total_count, list_of_data)
     */
    fun get(): List<Map<Select, Any>> {
        val rawData = getScannerDataRaw()
        val data = rawData.data

        val columns = query.columns ?: emptyList()
        val resultList = data.map { row ->
            val result = mutableMapOf<Select, Any>()
            result[DefaultSelects.Ticker] = row.s

            // Map the data array to column names
            row.d.forEachIndexed { index, value ->
                if (index < columns.size) {
                    result[columns[index]] = value
                }
            }
            result.toMap()
        }

        return resultList
    }

    /**
     * Create a copy of this query
     */
    fun copy(): Query {
        val newQuery = Query()
        newQuery.query = this.query.copy()
        newQuery.url = this.url
        return newQuery
    }

    /**
     * Get the current query configuration
     */
    fun getQuery(): QueryDict = query

    /**
     * Get the current URL
     */
    fun getUrl(): String = url


    override fun toString(): String {
        return "Query(query=$query, url='$url')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Query) return false
        return query == other.query && url == other.url
    }

    override fun hashCode(): Int {
        var result = query.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }
}

/**
 * Helper function to implement AND/OR chaining
 */
private fun implAndOrChaining(
    expressions: List<Any>, // FilterOperationDict or OperationDict
    operator: String
): OperationDict {
    val operands = expressions.map { expr ->
        when (expr) {
            is FilterOperationDict -> ExpressionDict(expr)
            else -> expr
        }
    }

    return OperationDict(
        OperationComparisonDict(
            operator = operator,
            operands = operands
        )
    )
}

/**
 * Combine multiple expressions with AND operator
 */
fun And(vararg expressions: Any): OperationDict {
    return implAndOrChaining(expressions.toList(), "and")
}

/**
 * Combine multiple expressions with OR operator
 */
fun Or(vararg expressions: Any): OperationDict {
    return implAndOrChaining(expressions.toList(), "or")
}
