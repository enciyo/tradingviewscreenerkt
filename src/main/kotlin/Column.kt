package com.enciyo

/**
 * A Column object represents a field in the tradingview stock screener,
 * and it's used in SELECT queries and WHERE queries with the `Query` object.
 *
 * A `Column` supports all the comparison operations:
 * `<`, `<=`, `>`, `>=`, `==`, `!=`, and also other methods like `between()`, `isin()`, etc.
 *
 * Examples:
 *
 * Some of the operations you can do:
 * ```
 * Column("close") > 2.5
 * Column("High.All") <= "high"
 * Column("high") > "VWAP"
 * Column("high") > Column("VWAP")  // same thing as above
 * Column("is_primary") == true
 * Column("exchange") != "OTC"
 *
 * Column("close").abovePct("VWAP", 1.03)
 * Column("close").abovePct("price_52_week_low", 2.5)
 * Column("close").belowPct("VWAP", 1.03)
 * Column("close").betweenPct("EMA200", 1.2, 1.5)
 * Column("close").notBetweenPct("EMA200", 1.2, 1.5)
 *
 * Column("close").between(2.5, 15.0)
 * Column("close").between("EMA5", "EMA20")
 *
 * Column("type").isin(listOf("stock", "fund"))
 * Column("exchange").isin(listOf("AMEX", "NASDAQ", "NYSE"))
 * Column("sector").notIn(listOf("Health Technology", "Health Services"))
 * Column("typespecs").has(listOf("common"))
 * Column("typespecs").hasNoneOf(listOf("reit", "etn", "etf"))
 *
 * Column("description").like("apple")  // the same as `description LIKE '%apple%'`
 * Column("premarket_change").notEmpty()  // same as `Column("premarket_change") != null`
 * Column("earnings_release_next_trading_date_fq").inDayRange(0, 0)  // same day
 * ```
 */
data class Column(val name: String) {
    
    companion object {
        /**
         * Extract the name from an object, whether it's a Column or a direct value
         */
        fun extractName(obj: Any?): Any? {
            return when (obj) {
                is Column -> obj.name
                else -> obj
            }
        }
    }

    // Comparison operators - using standard operator overloading
    operator fun compareTo(other: Any?): Int {
        // This is just for sorting purposes, not for filter operations
        return name.compareTo(other.toString())
    }

    // Comparison operators for filter operations
    fun greaterThan(other: Any?): FilterOperationDict {
        return when {
            other is Number -> FilterOperationDict(name, "greater", other)
            other is String -> FilterOperationDict(name, "greater", other)
            other is Column -> FilterOperationDict(name, "greater", other.name)
            else -> FilterOperationDict(name, "greater", other)
        }
    }

    fun lessThan(other: Any?): FilterOperationDict {
        return when {
            other is Number -> FilterOperationDict(name, "less", other)
            other is String -> FilterOperationDict(name, "less", other)
            other is Column -> FilterOperationDict(name, "less", other.name)
            else -> FilterOperationDict(name, "less", other)
        }
    }

    fun lessThanOrEqual(other: Any?): FilterOperationDict {
        return when {
            other is Number -> FilterOperationDict(name, "eless", other)
            other is String -> FilterOperationDict(name, "eless", other)
            other is Column -> FilterOperationDict(name, "eless", other.name)
            else -> FilterOperationDict(name, "eless", other)
        }
    }

    fun greaterThanOrEqual(other: Any?): FilterOperationDict {
        return when {
            other is Number -> FilterOperationDict(name, "egreater", other)
            other is String -> FilterOperationDict(name, "egreater", other)
            other is Column -> FilterOperationDict(name, "egreater", other.name)
            else -> FilterOperationDict(name, "egreater", other)
        }
    }

    fun equalTo(other: Any?): FilterOperationDict {
        return when {
            other is Number -> FilterOperationDict(name, "equal", other)
            other is String -> FilterOperationDict(name, "equal", other)
            other is Column -> FilterOperationDict(name, "equal", other.name)
            other == null -> FilterOperationDict(name, "empty", null)
            else -> FilterOperationDict(name, "equal", other)
        }
    }

    fun notEqualTo(other: Any?): FilterOperationDict {
        return when {
            other is Number -> FilterOperationDict(name, "nequal", other)
            other is String -> FilterOperationDict(name, "nequal", other)
            other is Column -> FilterOperationDict(name, "nequal", other.name)
            other == null -> FilterOperationDict(name, "nempty", null)
            else -> FilterOperationDict(name, "nequal", other)
        }
    }

    // Cross operations
    fun crosses(other: Any?): FilterOperationDict {
        return FilterOperationDict(name, "crosses", extractName(other))
    }

    fun crossesAbove(other: Any?): FilterOperationDict {
        return FilterOperationDict(name, "crosses_above", extractName(other))
    }

    fun crossesBelow(other: Any?): FilterOperationDict {
        return FilterOperationDict(name, "crosses_below", extractName(other))
    }

    // Range operations
    fun between(left: Any?, right: Any?): FilterOperationDict {
        return FilterOperationDict(
            left = name,
            operation = "in_range",
            right = listOf(extractName(left), extractName(right))
        )
    }

    fun notBetween(left: Any?, right: Any?): FilterOperationDict {
        return FilterOperationDict(
            left = name,
            operation = "not_in_range",
            right = listOf(extractName(left), extractName(right))
        )
    }

    // Collection operations
    fun isin(values: Iterable<*>): FilterOperationDict {
        return FilterOperationDict(name, "in_range", values.toList())
    }

    fun notIn(values: Iterable<*>): FilterOperationDict {
        return FilterOperationDict(name, "not_in_range", values.toList())
    }

    fun has(values: List<String>): FilterOperationDict {
        /**
         * Field contains any of the values
         *
         * (it's the same as `isin()`, except that it works on fields of type `set`)
         */
        return FilterOperationDict(name, "has", values)
    }

    fun hasNoneOf(values: List<String>): FilterOperationDict {
        /**
         * Field doesn't contain any of the values
         *
         * (it's the same as `not_in()`, except that it works on fields of type `set`)
         */
        return FilterOperationDict(name, "has_none_of", values)
    }

    // Time range operations
    fun inDayRange(a: Int, b: Int): FilterOperationDict {
        return FilterOperationDict(name, "in_day_range", listOf(a, b))
    }

    fun inWeekRange(a: Int, b: Int): FilterOperationDict {
        return FilterOperationDict(name, "in_week_range", listOf(a, b))
    }

    fun inMonthRange(a: Int, b: Int): FilterOperationDict {
        return FilterOperationDict(name, "in_month_range", listOf(a, b))
    }

    // Percentage operations
    fun abovePct(column: Any?, pct: Double): FilterOperationDict {
        /**
         * Examples:
         *
         * The closing price is higher than the VWAP by more than 3%
         * ```
         * Column("close").abovePct("VWAP", 1.03)
         * ```
         *
         * closing price is above the 52-week-low by more than 150%
         * ```
         * Column("close").abovePct("price_52_week_low", 2.5)
         * ```
         */
        return FilterOperationDict(
            left = name,
            operation = "above%",
            right = listOf(extractName(column), pct)
        )
    }

    fun belowPct(column: Any?, pct: Double): FilterOperationDict {
        /**
         * Examples:
         *
         * The closing price is lower than the VWAP by 3% or more
         * ```
         * Column("close").belowPct("VWAP", 1.03)
         * ```
         */
        return FilterOperationDict(
            left = name,
            operation = "below%",
            right = listOf(extractName(column), pct)
        )
    }

    fun betweenPct(
        column: Any?,
        pct1: Double,
        pct2: Double? = null
    ): FilterOperationDict {
        /**
         * Examples:
         *
         * The percentage change between the Close and the EMA is between 20% and 50%
         * ```
         * Column("close").betweenPct("EMA200", 1.2, 1.5)
         * ```
         */
        return FilterOperationDict(
            left = name,
            operation = "in_range%",
            right = if (pct2 != null) listOf(extractName(column), pct1, pct2) else listOf(extractName(column), pct1)
        )
    }

    fun notBetweenPct(
        column: Any?,
        pct1: Double,
        pct2: Double? = null
    ): FilterOperationDict {
        /**
         * Examples:
         *
         * The percentage change between the Close and the EMA is between 20% and 50%
         * ```
         * Column("close").notBetweenPct("EMA200", 1.2, 1.5)
         * ```
         */
        return FilterOperationDict(
            left = name,
            operation = "not_in_range%",
            right = if (pct2 != null) listOf(extractName(column), pct1, pct2) else listOf(extractName(column), pct1)
        )
    }

    // String operations
    fun like(other: Any?): FilterOperationDict {
        return FilterOperationDict(name, "match", extractName(other))
    }

    fun notLike(other: Any?): FilterOperationDict {
        return FilterOperationDict(name, "nmatch", extractName(other))
    }

    // Null/empty operations
    fun empty(): FilterOperationDict {
        // it seems like the `right` key is optional
        return FilterOperationDict(name, "empty", null)
    }

    fun notEmpty(): FilterOperationDict {
        /**
         * This method can be used to check if a field is not null.
         */
        return FilterOperationDict(name, "nempty", null)
    }

    override fun toString(): String {
        return "< Column(${name}) >"
    }
}
