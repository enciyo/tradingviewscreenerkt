package com.enciyo

enum class DefaultSelects(
    override val value: String
) : Select {
    Name("name"),

    @Deprecated("Use Name instead")
    Ticker("ticker");

    companion object {
        fun all() = entries
    }
}