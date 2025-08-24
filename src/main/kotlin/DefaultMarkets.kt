package com.enciyo

enum class DefaultMarkets(
    override val value: String
) : Market {
    America("america"),
    Turkey("turkey");

    companion object {
        fun all() = entries.map { it.value }
    }
}