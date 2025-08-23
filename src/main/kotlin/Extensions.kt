package com.enciyo

// Extension functions for operator overloading
operator fun Column.compareTo(other: Any?): Int {
    return this.name.compareTo(other.toString())
}

operator fun Column.plus(other: Any?): FilterOperationDict {
    return FilterOperationDict(this.name, "plus", Column.extractName(other))
}

operator fun Column.minus(other: Any?): FilterOperationDict {
    return FilterOperationDict(this.name, "minus", Column.extractName(other))
}

operator fun Column.times(other: Any?): FilterOperationDict {
    return FilterOperationDict(this.name, "times", Column.extractName(other))
}

operator fun Column.div(other: Any?): FilterOperationDict {
    return FilterOperationDict(this.name, "div", Column.extractName(other))
}

operator fun Column.rem(other: Any?): FilterOperationDict {
    return FilterOperationDict(this.name, "rem", Column.extractName(other))
}

// Type alias for convenience
typealias col = Column
