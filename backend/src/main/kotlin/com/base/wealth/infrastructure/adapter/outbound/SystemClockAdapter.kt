package com.base.wealth.infrastructure.adapter.outbound

import com.base.wealth.domain.port.outbound.ClockPort
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SystemClockAdapter : ClockPort {
    override fun now(): Instant = Instant.now()
}
