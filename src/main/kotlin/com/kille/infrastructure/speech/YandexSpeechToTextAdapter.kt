package com.kille.infrastructure.speech

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kille.application.port.output.SpeechToTextPort
import com.kille.application.port.output.WordTiming
import com.kille.config.YandexSpeechProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.HttpClientErrorException

@Component
class YandexSpeechToTextAdapter(
    private val properties: YandexSpeechProperties,
    private val objectMapper: ObjectMapper
) : SpeechToTextPort {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val restClient: RestClient = RestClient.builder().build()

    override fun recognizeWords(audioUrl: String): List<WordTiming> {
        require(audioUrl.isNotBlank()) { "Audio URL is required" }
        require(properties.apiKey.isNotBlank()) { "SpeechKit API key is not configured" }

        logger.debug("Recognizing speech from URL: {}", audioUrl)
        logger.debug("Using SpeechKit endpoint: {}", properties.endpoint)
        logger.debug("Language: {}, Model: {}, AudioContainer: {}", properties.language, properties.model, properties.audioContainer)

        val requestBody = mutableMapOf<String, Any>(
            "uri" to audioUrl,
            "recognitionModel" to mapOf(
                "model" to properties.model,
                "languageRestriction" to mapOf(
                    "restrictionType" to "WHITELIST",
                    "languageCode" to listOf(properties.language)
                ),
                "audioFormat" to mapOf(
                    "containerAudio" to mapOf("containerAudioType" to properties.audioContainer)
                )
            ),
            "rawResults" to true
        )
        if (properties.folderId.isNotBlank()) {
            requestBody["folderId"] = properties.folderId
        }

        val rawResponse = try {
            val request = restClient.post()
                .uri(properties.endpoint)
                .header(HttpHeaders.AUTHORIZATION, "Api-Key ${properties.apiKey}")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)

            if (properties.folderId.isNotBlank()) {
                request.header("x-folder-id", properties.folderId)
            }

            request
                .retrieve()
                .body(String::class.java)
                ?: throw IllegalStateException("Empty SpeechKit response")
        } catch (ex: HttpClientErrorException) {
            logger.error("SpeechKit API error (HTTP {}): {}", ex.statusCode, ex.responseBodyAsString, ex)
            throw IllegalStateException("Failed to recognize speech via SpeechKit: ${ex.statusCode} - ${ex.responseBodyAsString}", ex)
        } catch (ex: RestClientException) {
            logger.error("SpeechKit request failed for URL: {}", audioUrl, ex)
            throw IllegalStateException("Failed to recognize speech via SpeechKit: ${ex.message}", ex)
        }

        val root = objectMapper.readTree(rawResponse)
        val words = mutableListOf<WordTiming>()
        collectWords(root, words)

        logger.debug("SpeechKit returned {} words from response", words.size)

        if (words.isEmpty()) {
            logger.warn("SpeechKit returned no word timings. Raw response: {}", rawResponse)
            throw IllegalStateException("SpeechKit returned no word timings")
        }

        return words.sortedBy { it.startMs }
    }

    private fun collectWords(node: JsonNode, result: MutableList<WordTiming>) {
        if (node.isObject) {
            if (node.has("words") && node.get("words").isArray) {
                node.get("words").forEach { wordNode ->
                    parseWord(wordNode)?.let(result::add)
                }
            }

            node.fields().forEachRemaining { (_, child) ->
                collectWords(child, result)
            }
            return
        }

        if (node.isArray) {
            node.forEach { child -> collectWords(child, result) }
        }
    }

    private fun parseWord(node: JsonNode): WordTiming? {
        val word = firstText(node, "word", "text", "value")?.trim().orEmpty()
        val startMs = firstLong(node, "startMs", "startTimeMs", "start_time_ms")
            ?: parseDurationToMs(firstText(node, "startTime", "start_time"))
        val endMs = firstLong(node, "endMs", "endTimeMs", "end_time_ms")
            ?: parseDurationToMs(firstText(node, "endTime", "end_time"))

        if (word.isBlank() || startMs == null || endMs == null || endMs < startMs) {
            return null
        }

        return WordTiming(word = word, startMs = startMs, endMs = endMs)
    }

    private fun firstText(node: JsonNode, vararg keys: String): String? {
        return keys.asSequence()
            .mapNotNull { key -> node.get(key)?.takeIf { !it.isNull }?.asText() }
            .firstOrNull()
    }

    private fun firstLong(node: JsonNode, vararg keys: String): Long? {
        return keys.asSequence()
            .mapNotNull { key ->
                val valueNode = node.get(key) ?: return@mapNotNull null
                if (valueNode.isNumber) {
                    valueNode.longValue()
                } else {
                    valueNode.asText().toLongOrNull()
                }
            }
            .firstOrNull()
    }

    private fun parseDurationToMs(raw: String?): Long? {
        if (raw.isNullOrBlank()) {
            return null
        }

        return when {
            raw.endsWith("ms") -> raw.removeSuffix("ms").trim().toLongOrNull()
            raw.endsWith("s") -> {
                val seconds = raw.removeSuffix("s").trim().toDoubleOrNull() ?: return null
                (seconds * 1000).toLong()
            }
            else -> raw.toLongOrNull()
        }
    }
}