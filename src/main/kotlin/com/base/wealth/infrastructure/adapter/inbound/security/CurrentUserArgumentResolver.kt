package com.base.wealth.infrastructure.adapter.inbound.security

import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.util.UUID

/**
 * Resolves to the raw [UUID], not [com.base.wealth.domain.model.UserId] directly: Spring's
 * Kotlin-reflection method invoker (InvocableHandlerMethod$KotlinDelegate) mishandles a
 * @JvmInline value class returned from a resolver overriding this Java interface — the boxed
 * value reaches the target constructor as null. Controllers wrap `UserId(userId)` themselves;
 * every call into a use case still only accepts UserId, so a forgotten wrap still won't compile.
 */
@Component
class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(CurrentUser::class.java) && parameter.parameterType == UUID::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): UUID {
        val jwt =
            SecurityContextHolder.getContext().authentication?.principal as? Jwt
                ?: error("@CurrentUser used on an endpoint with no authenticated JWT principal")
        return UUID.fromString(jwt.subject)
    }
}
