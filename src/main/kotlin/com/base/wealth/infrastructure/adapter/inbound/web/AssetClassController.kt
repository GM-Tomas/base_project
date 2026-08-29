package com.base.wealth.infrastructure.adapter.inbound.web

import com.base.wealth.application.dto.AvailableAssetClassesResponse
import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.AssetClassUseCase
import com.base.wealth.infrastructure.adapter.inbound.security.CurrentUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/asset-classes")
@Tag(name = "Asset classes", description = "Clases de activo sugeridas y en uso por el usuario")
class AssetClassController(
    private val assetClassUseCase: AssetClassUseCase,
) {
    @GetMapping
    @Operation(summary = "Lista las clases de activo por defecto, en uso, y la unión de ambas")
    fun getAvailableAssetClasses(
        @CurrentUser userId: UUID,
    ): ResponseEntity<AvailableAssetClassesResponse> {
        val result = assetClassUseCase.getAvailableAssetClasses(UserId(userId))
        return ResponseEntity.ok(AvailableAssetClassesResponse(result.defaults, result.inUse, result.all))
    }
}
