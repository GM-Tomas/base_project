package com.base.wealth.application.dto

data class AvailableAssetClassesResponse(
    val defaults: List<String>,
    val inUse: List<String>,
    val all: List<String>,
)
