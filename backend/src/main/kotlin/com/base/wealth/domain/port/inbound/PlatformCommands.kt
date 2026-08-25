package com.base.wealth.domain.port.inbound

import com.base.wealth.domain.model.UserId

data class CreatePlatformCommand(
    val userId: UserId,
    val name: String,
    val type: String = "Other",
)

data class PatchPlatformCommand(
    val userId: UserId,
    val name: String,
    val newName: String? = null,
    val newType: String? = null,
)
