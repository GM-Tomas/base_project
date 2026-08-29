package com.base.wealth.domain.model

private val INTERNAL_WHITESPACE = Regex("\\s+")

/**
 * Trim + collapse internal whitespace, then enforce a length bound.
 * Shared by the free-text label value classes below.
 */
internal fun normalizeLabel(
    raw: String,
    maxLength: Int,
    fieldName: String,
): String {
    val normalized = raw.trim().replace(INTERNAL_WHITESPACE, " ")
    require(normalized.isNotEmpty()) { "$fieldName must not be blank" }
    require(normalized.length <= maxLength) { "$fieldName must not exceed $maxLength characters" }
    return normalized
}
