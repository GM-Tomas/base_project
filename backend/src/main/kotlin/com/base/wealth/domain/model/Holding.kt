package com.base.wealth.domain.model

data class Holding(
    val id: Long,
    val name: String,
    val cls: AssetClass,
    val platform: String,
    val qty: Double? = null,
    val value: Double // Almacenado en USD
)
