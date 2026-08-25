package com.base.wealth.application.service

import com.base.wealth.domain.model.Platform
import com.base.wealth.domain.model.PlatformName
import com.base.wealth.domain.model.PlatformType
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.CreatePlatformCommand
import com.base.wealth.domain.port.inbound.PatchPlatformCommand
import com.base.wealth.domain.port.inbound.PlatformUseCase
import com.base.wealth.domain.port.outbound.ClockPort
import com.base.wealth.domain.port.outbound.PlatformRepository
import com.base.wealth.exception.DuplicateResourceException
import com.base.wealth.exception.ResourceInUseException
import com.base.wealth.exception.ResourceNotFoundException
import org.springframework.stereotype.Service

@Service
class PlatformService(
    private val platformRepository: PlatformRepository,
    private val clock: ClockPort,
) : PlatformUseCase {
    override fun getAllPlatforms(userId: UserId): List<Platform> = platformRepository.findAll(userId)

    override fun createPlatform(command: CreatePlatformCommand): Platform {
        val name = PlatformName.of(command.name)
        if (platformRepository.findByName(command.userId, name) != null) {
            throw DuplicateResourceException("Platform '${command.name}' already exists")
        }
        val platform = Platform(command.userId, name, PlatformType.of(command.type), clock.now())
        return platformRepository.save(platform)
    }

    override fun patchPlatform(command: PatchPlatformCommand): Platform {
        val currentName = PlatformName.of(command.name)
        val newName = command.newName?.let { PlatformName.of(it) }
        if (newName != null &&
            !newName.value.equals(command.name, ignoreCase = true) &&
            platformRepository.findByName(command.userId, newName) != null
        ) {
            throw DuplicateResourceException("Platform '${command.newName}' already exists")
        }
        return platformRepository.update(
            command.userId,
            currentName,
            newName,
            command.newType?.let { PlatformType.of(it) },
        ) ?: throw ResourceNotFoundException("No se encontró la plataforma: ${command.name}")
    }

    override fun deletePlatform(
        userId: UserId,
        name: String,
    ) {
        val platformName = PlatformName.of(name)
        val holdingsCount = platformRepository.countHoldings(userId, platformName)
        if (holdingsCount > 0) {
            throw ResourceInUseException("Platform '$name' still has $holdingsCount holdings")
        }
        if (!platformRepository.deleteByName(userId, platformName)) {
            throw ResourceNotFoundException("No se encontró la plataforma: $name")
        }
    }
}
