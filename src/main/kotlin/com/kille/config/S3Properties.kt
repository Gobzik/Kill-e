// src/main/kotlin/com/kille/config/S3Properties.kt
package com.kille.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.bind.ConstructorBinding

@Validated
@ConfigurationProperties(prefix = "yandex.cloud.storage")
data class S3Properties @ConstructorBinding constructor(
    @field:NotBlank
    val endpoint: String = "https://storage.yandexcloud.net",

    @field:NotBlank
    val region: String = "ru-central1",

    @field:NotBlank
    val accessKey: String,

    @field:NotBlank
    val secretKey: String,

    @field:NotBlank
    val bucket: String,

    val presignedUrlExpirationHours: Long = 1
)