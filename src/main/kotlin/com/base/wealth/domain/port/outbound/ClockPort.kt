package com.base.wealth.domain.port.outbound

import java.time.Instant

/** The current instant is I/O, not a pure function — injected so tests can pin it. */
interface ClockPort {
    fun now(): Instant
}
