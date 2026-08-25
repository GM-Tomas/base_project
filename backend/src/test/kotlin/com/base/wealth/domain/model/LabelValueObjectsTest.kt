package com.base.wealth.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class LabelValueObjectsTest {
    @Test
    @DisplayName("trims and collapses internal whitespace")
    fun normalizesWhitespace() {
        assertEquals("Fixed Income", AssetClass.of("  Fixed   Income  ").value)
    }

    @Test
    @DisplayName("blank input is rejected")
    fun rejectsBlank() {
        assertThrows(IllegalArgumentException::class.java) { AssetClass.of("   ") }
    }

    @Test
    @DisplayName("comparison is case-sensitive — 'Crypto' and 'crypto' are different classes")
    fun assetClassIsCaseSensitive() {
        assert(AssetClass.of("Crypto") != AssetClass.of("crypto"))
    }

    @Test
    @DisplayName("too long a name is rejected")
    fun rejectsOverLength() {
        assertThrows(IllegalArgumentException::class.java) { PlatformName.of("x".repeat(121)) }
    }

    @Test
    @DisplayName("PlatformType.OTHER is the alta implícita default")
    fun platformTypeDefault() {
        assertEquals("Other", PlatformType.OTHER.value)
    }
}
