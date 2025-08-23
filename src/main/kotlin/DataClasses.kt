package com.enciyo

/**
 * Data class representing a filter operation dictionary
 */
data class FilterOperationDict(
    val left: String,
    val operation: String,
    val right: Any?
)

/**
 * Data class representing sort by configuration
 */
data class SortByDict(
    val sortBy: String,
    val sortOrder: String, // "asc" or "desc"
    val nullsFirst: Boolean? = null
)

/**
 * Data class representing symbols configuration
 */
data class SymbolsDict(
    val query: Map<String, List<String>>? = null,
    val tickers: List<String>? = null,
    val symbolset: List<String>? = null,
    val watchlist: Map<String, Int>? = null,
    val groups: List<Map<String, String>>? = null
)

/**
 * Data class representing an expression
 */
data class ExpressionDict(
    val expression: FilterOperationDict
)

/**
 * Data class representing operation comparison
 */
data class OperationComparisonDict(
    val operator: String, // "and" or "or"
    val operands: List<Any> // OperationDict or ExpressionDict
)

/**
 * Data class representing an operation
 */
data class OperationDict(
    val operation: OperationComparisonDict
)

/**
 * Data class representing the main query structure
 */
data class QueryDict(
    val markets: List<String>? = null,
    val symbols: SymbolsDict? = null,
    val options: Map<String, Any>? = null,
    val columns: List<String>? = null,
    val filter: List<FilterOperationDict>? = null,
    val filter2: OperationComparisonDict? = null,
    val sort: SortByDict? = null,
    val range: List<Int>? = null,
    val ignoreUnknownFields: Boolean = false,
    val preset: String? = null,
    val priceConversion: Map<String, Any>? = null
)

/**
 * Data class representing a screener row
 */
data class ScreenerRowDict(
    val s: String, // symbol (NASDAQ:AAPL)
    val d: List<Any> // data, list of values
)

/**
 * Data class representing the screener response
 */
data class ScreenerDict(
    val totalCount: Int,
    val data: List<ScreenerRowDict>
)
