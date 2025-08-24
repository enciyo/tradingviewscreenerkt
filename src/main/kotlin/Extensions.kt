package com.enciyo

// Extension functions for operator overloading
operator fun Select.compareTo(other: Any?): Int {
    return this.value.compareTo(other.toString())
}

operator fun Select.plus(other: Any?): FilterOperationDict {
    return FilterOperationDict(this.value, "plus", Select.extractName(other))
}

operator fun Select.minus(other: Any?): FilterOperationDict {
    return FilterOperationDict(this.value, "minus", Select.extractName(other))
}

operator fun Select.times(other: Any?): FilterOperationDict {
    return FilterOperationDict(this.value, "times", Select.extractName(other))
}

operator fun Select.div(other: Any?): FilterOperationDict {
    return FilterOperationDict(this.value, "div", Select.extractName(other))
}

operator fun Select.rem(other: Any?): FilterOperationDict {
    return FilterOperationDict(this.value, "rem", Select.extractName(other))
}

