// src/main/kotlin/com/kille/config/S3Properties.kt
package com.kille.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.NotBlank

@Validated
@ConfigurationProperties(prefix = "yandex.cloud.storage")
data class S3Properties(
    @field:NotBlank
    val endpoint: String = "https://storage.yandexcloud.net",

    @field:NotBlank
    val region: String = "ru-central1",

    val accessKey: String = "",

    val secretKey: String = "",

    val bucket: String = "",

    @field:NotBlank
    val chaptersPrefix: String = "books",

    val pathStyleAccessEnabled: Boolean = true,

    val presignedUrlExpirationHours: Long = 1
)