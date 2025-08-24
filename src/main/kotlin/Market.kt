package com.enciyo

interface Market {
    val value: String
}

fun market(value: String) = object : Market {
    override val value: String = value
}

