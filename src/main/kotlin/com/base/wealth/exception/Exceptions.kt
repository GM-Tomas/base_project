package com.base.wealth.exception

class ResourceNotFoundException(
    message: String,
) : RuntimeException(message)

/** A unique-name clash (e.g. platform name already in use for this user, case-insensitively). */
class DuplicateResourceException(
    message: String,
) : RuntimeException(message)

/** The resource can't be deleted because something else still references it (e.g. a platform with holdings). */
class ResourceInUseException(
    message: String,
) : RuntimeException(message)
