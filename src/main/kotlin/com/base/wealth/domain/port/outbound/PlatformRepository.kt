package com.base.wealth.domain.port.outbound

import com.base.wealth.domain.model.Platform
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.PlatformType
import com.base.wealth.domain.model.UserId
import java.time.Instant

interface PlatformRepository {
    fun findAll(userId: UserId): List<Platform>

    fun findByName(
        userId: UserId,
        name: PlatformName,
    ): Platform?

    /**
     * Case-insensitive alta implícita (CA-02.2): creates the platform if missing, returns the
     * canonical name either way.
     */
    fun ensureExists(
        userId: UserId,
        name: PlatformName,
        now: Instant,
    ): PlatformName

    fun save(platform: Platform): Platform

    /**
     * Updates name and/or type in place — a rename cascades to holdings.platform_name
     * (ON UPDATE CASCADE, data-model.md §2). `null` if [currentName] doesn't exist for [userId].
     */
    fun update(
        userId: UserId,
        currentName: PlatformName,
        newName: PlatformName?,
        newType: PlatformType?,
    ): Platform?

    fun deleteByName(
        userId: UserId,
        name: PlatformName,
    ): Boolean

    fun countHoldings(
        userId: UserId,
        name: PlatformName,
    ): Int
}
