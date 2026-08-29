package com.base.wealth.domain.model

/** Free text, not a closed enum — e.g. "Broker", "Wallet", "Exchange", or whatever the user typed. */
@JvmInline
value class PlatformType private constructor(
    val value: String,
) {
    companion object {
        private const val MAX_LENGTH = 40
        val OTHER = PlatformType("Other")

        fun of(raw: String): PlatformType = PlatformType(normalizeLabel(raw, MAX_LENGTH, "PlatformType"))
    }
}
