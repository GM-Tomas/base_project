package com.base.wealth.infrastructure.adapter.inbound.security

/**
 * Resolves a controller parameter of type [com.base.wealth.domain.model.UserId] from the
 * verified JWT's `sub` claim (see [CurrentUserArgumentResolver]). The identity comes from the
 * token, never from a request body/path/query — see specs/001-backend-para-frontend/spec.md CA-01.5.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUser
