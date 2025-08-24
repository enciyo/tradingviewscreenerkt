package com.enciyo

interface Select {
    val value: String


    companion object {
        /**
         * Extract the name from an object, whether it's a Column or a direct value
         */
        fun extractName(obj: Any?): Any? {
            return when (obj) {
                is Select -> obj.value
                else -> obj
            }
        }
    }

    // Comparison operators - using standard operator overloading
    operator fun compareTo(other: Any?): Int {
        // This is just for sorting purposes, not for filter operations
        return value.compareTo(other.toString())
    }

    // Comparison operators for filter operations
    fun greaterThan(other: Any?): FilterOperationDict {
        return when {
            other is Number -> FilterOperationDict(value, "greater", other)
            other is String -> FilterOperationDict(value, "greater", other)
            other is Select -> FilterOperationDict(value, "greater", other.value)
            else -> FilterOperationDict(value, "greater", other)
        }
    }

    fun lessThan(other: Any?): FilterOperationDict {
        return when {
            other is Number -> FilterOperationDict(value, "less", other)
            other is String -> FilterOperationDict(value, "less", other)
            other is Select -> FilterOperationDict(value, "less", other.value)
            else -> FilterOperationDict(value, "less", other)
        }
    }

    fun lessThanOrEqual(other: Any?): FilterOperationDict {
        return when {
            other is Number -> FilterOperationDict(value, "eless", other)
            other is String -> FilterOperationDict(value, "eless", other)
            other is Select -> FilterOperationDict(value, "eless", other.value)
            else -> FilterOperationDict(value, "eless", other)
        }
    }

    fun greaterThanOrEqual(other: Any?): FilterOperationDict {
        return when {
            other is Number -> FilterOperationDict(value, "egreater", other)
            other is String -> FilterOperationDict(value, "egreater", other)
            other is Select -> FilterOperationDict(value, "egreater", other.value)
            else -> FilterOperationDict(value, "egreater", other)
        }
    }

    fun equalTo(other: Any?): FilterOperationDict {
        return when {
            other is Number -> FilterOperationDict(value, "equal", other)
            other is String -> FilterOperationDict(value, "equal", other)
            other is Select -> FilterOperationDict(value, "equal", other.value)
            other == null -> FilterOperationDict(value, "empty", null)
            else -> FilterOperationDict(value, "equal", other)
        }
    }

    fun notEqualTo(other: Any?): FilterOperationDict {
        return when {
            other is Number -> FilterOperationDict(value, "nequal", other)
            other is String -> FilterOperationDict(value, "nequal", other)
            other is Select -> FilterOperationDict(value, "nequal", other.value)
            other == null -> FilterOperationDict(value, "nempty", null)
            else -> FilterOperationDict(value, "nequal", other)
        }
    }

    // Cross operations
    fun crosses(other: Any?): FilterOperationDict {
        return FilterOperationDict(value, "crosses", extractName(other))
    }

    fun crossesAbove(other: Any?): FilterOperationDict {
        return FilterOperationDict(value, "crosses_above", extractName(other))
    }

    fun crossesBelow(other: Any?): FilterOperationDict {
        return FilterOperationDict(value, "crosses_below", extractName(other))
    }

    // Range operations
    fun between(left: Any?, right: Any?): FilterOperationDict {
        return FilterOperationDict(
            left = value,
            operation = "in_range",
            right = listOf(extractName(left), extractName(right))
        )
    }

    fun notBetween(left: Any?, right: Any?): FilterOperationDict {
        return FilterOperationDict(
            left = value,
            operation = "not_in_range",
            right = listOf(extractName(left), extractName(right))
        )
    }

    // Collection operations
    fun isin(values: Iterable<*>): FilterOperationDict {
        return FilterOperationDict(value, "in_range", values.toList())
    }

    fun notIn(values: Iterable<*>): FilterOperationDict {
        return FilterOperationDict(value, "not_in_range", values.toList())
    }

    fun has(values: List<String>): FilterOperationDict {
        /**
         * Field contains any of the values
         *
         * (it's the same as `isin()`, except that it works on fields of type `set`)
         */
        return FilterOperationDict(value, "has", values)
    }

    fun hasNoneOf(values: List<String>): FilterOperationDict {
        /**
         * Field doesn't contain any of the values
         *
         * (it's the same as `not_in()`, except that it works on fields of type `set`)
         */
        return FilterOperationDict(value, "has_none_of", values)
    }

    // Time range operations
    fun inDayRange(a: Int, b: Int): FilterOperationDict {
        return FilterOperationDict(value, "in_day_range", listOf(a, b))
    }

    fun inWeekRange(a: Int, b: Int): FilterOperationDict {
        return FilterOperationDict(value, "in_week_range", listOf(a, b))
    }

    fun inMonthRange(a: Int, b: Int): FilterOperationDict {
        return FilterOperationDict(value, "in_month_range", listOf(a, b))
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
            left = value,
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
            left = value,
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
            left = value,
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
            left = value,
            operation = "not_in_range%",
            right = if (pct2 != null) listOf(extractName(column), pct1, pct2) else listOf(extractName(column), pct1)
        )
    }

    // String operations
    fun like(other: Any?): FilterOperationDict {
        return FilterOperationDict(value, "match", extractName(other))
    }

    fun notLike(other: Any?): FilterOperationDict {
        return FilterOperationDict(value, "nmatch", extractName(other))
    }

    // Null/empty operations
    fun empty(): FilterOperationDict {
        // it seems like the `right` key is optional
        return FilterOperationDict(value, "empty", null)
    }

    fun notEmpty(): FilterOperationDict {
        /**
         * This method can be used to check if a field is not null.
         */
        return FilterOperationDict(value, "nempty", null)
    }

}

fun select(name: String): Select {
    return object : Select {
        override val value: String
            get() = name
    }
}