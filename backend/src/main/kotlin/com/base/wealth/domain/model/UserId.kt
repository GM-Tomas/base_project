package com.base.wealth.domain.model

import java.util.UUID

/**
 * The Supabase Auth user id (`auth.users.id`, the JWT's `sub` claim). Never accepted from a
 * request body/path/query — always derived from the verified token (see security/CurrentUser.kt).
 */
@JvmInline
value class UserId(
    val value: UUID,
)
