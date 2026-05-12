package com.kille.infrastructure.storage.exception

class StorageOperationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

