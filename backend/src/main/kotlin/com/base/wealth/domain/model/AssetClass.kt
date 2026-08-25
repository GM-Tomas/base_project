package com.base.wealth.domain.model

/** Free text, not a closed enum — the frontend lets users create their own (F9/F10). Case-sensitive. */
@JvmInline
value class AssetClass private constructor(
    val value: String,
) {
    companion object {
        private const val MAX_LENGTH = 60

        fun of(raw: String): AssetClass = AssetClass(normalizeLabel(raw, MAX_LENGTH, "AssetClass"))
    }
}
