package com.base.wealth.infrastructure.adapter.inbound.web.error

import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import java.net.URI

const val TRACE_ID_MDC_KEY = "traceId"
private const val PROBLEM_BASE_URI = "https://base.wealth/errors"

/**
 * RFC 9457 problem, with the current request's traceId.
 * @see com.base.wealth.infrastructure.config.RequestIdFilter
 */
fun problemDetailOf(
    status: HttpStatus,
    slug: String,
    detail: String?,
): ProblemDetail {
    val problem = ProblemDetail.forStatusAndDetail(status, detail ?: status.reasonPhrase)
    problem.type = URI.create("$PROBLEM_BASE_URI/$slug")
    problem.title = status.reasonPhrase
    problem.setProperty("traceId", MDC.get(TRACE_ID_MDC_KEY))
    return problem
}
