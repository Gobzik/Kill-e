package com.kille.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "yandex.cloud.speechkit")
data class YandexSpeechProperties(
    val endpoint: String = "https://stt.api.cloud.yandex.net/stt/v3/recognizeFileAsync",
    val apiKey: String = "",
    val language: String = "ru-RU",
    val model: String = "general",
    val audioContainer: String = "MP3",
    val folderId: String = ""
)