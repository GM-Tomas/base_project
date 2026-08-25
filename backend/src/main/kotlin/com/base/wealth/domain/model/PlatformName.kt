package com.base.wealth.domain.model

/** The natural key for a [Platform] (unique per user, case-insensitively — see data-model.md §2). */
@JvmInline
value class PlatformName private constructor(
    val value: String,
) {
    companion object {
        private const val MAX_LENGTH = 120

        fun of(raw: String): PlatformName = PlatformName(normalizeLabel(raw, MAX_LENGTH, "PlatformName"))
    }
}
