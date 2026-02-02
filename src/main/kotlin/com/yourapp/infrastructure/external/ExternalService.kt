package com.yourapp.infrastructure.external

interface ExternalService {
    suspend fun callExternalApi(params: Map<String, Any>): Result<String>
}
