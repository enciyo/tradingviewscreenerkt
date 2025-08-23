package com.enciyo

/**
 * Formats technical rating to human-readable string
 * 
 * @param rating Technical rating value as float
 * @return Formatted rating string
 * 
 * @see https://github.com/shner-elmo/TradingView-Screener/issues/12
 */
fun formatTechnicalRating(rating: Float): String {
    return when {
        rating >= 0.5f -> "Strong Buy"
        rating >= 0.1f -> "Buy"
        rating >= -0.1f -> "Neutral"
        rating >= -0.5f -> "Sell"
        else -> "Strong Sell"
    }
}
