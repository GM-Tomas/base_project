package com.base.wealth.domain.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class AssetClass {
    @JsonProperty("Cash")
    CASH,
    @JsonProperty("Fixed Income")
    FIXED_INCOME,
    @JsonProperty("Index Fund")
    INDEX_FUND,
    @JsonProperty("Equity")
    EQUITY,
    @JsonProperty("Crypto")
    CRYPTO
}

enum class PlatformType {
    @JsonProperty("Broker")
    BROKER,
    @JsonProperty("Wallet")
    WALLET,
    @JsonProperty("Bank")
    BANK,
    @JsonProperty("Exchange")
    EXCHANGE
}

enum class Currency {
    @JsonProperty("USD")
    USD,
    @JsonProperty("ARS")
    ARS,
    @JsonProperty("BTC")
    BTC,
    @JsonProperty("ETH")
    ETH,
    @JsonProperty("USDT")
    USDT
}
