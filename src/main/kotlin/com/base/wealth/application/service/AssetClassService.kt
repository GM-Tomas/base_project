package com.base.wealth.application.service

import com.base.wealth.domain.model.UserId
import com.base.wealth.domain.port.inbound.AssetClassUseCase
import com.base.wealth.domain.port.inbound.AvailableAssetClasses
import com.base.wealth.domain.port.outbound.HoldingRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

// ponytail: defaultAssetClasses stays on @Value, not WealthProperties (infrastructure/config) —
// same reasoning as WealthQueryService.fxRateUSDARS: this is `application`, and depending on an
// infrastructure class here is exactly the layer leak plan.md §8.2 checks for.
@Service
class AssetClassService(
    private val holdingRepository: HoldingRepository,
    @Value("\${wealth.default-asset-classes:Cash,Fixed Income,Index Fund,Equity,Crypto}")
    private val defaultAssetClasses: List<String>,
) : AssetClassUseCase {
    override fun getAvailableAssetClasses(userId: UserId): AvailableAssetClasses {
        val inUse = holdingRepository.assetClassesInUse(userId).map { it.value }
        val all = (defaultAssetClasses + inUse).distinct()
        return AvailableAssetClasses(defaultAssetClasses, inUse, all)
    }
}
