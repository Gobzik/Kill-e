package com.kille.infrastructure.speech

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.kille.application.port.output.SpeechToTextPort
import com.kille.application.port.output.WordTiming
import com.kille.config.YandexSpeechProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class YandexSpeechToTextAdapter(
    private val properties: YandexSpeechProperties,
    private val objectMapper: ObjectMapper
) : SpeechToTextPort {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val httpClient: HttpClient = HttpClient.newBuilder().build()

    override fun recognizeWords(audioUrl: String): List<WordTiming> {
        require(audioUrl.isNotBlank()) { "Audio URL is required" }
        require(properties.apiKey.isNotBlank()) { "SpeechKit API key is not configured" }

        logger.debug("Recognizing speech from URL: {}", audioUrl)
        logger.debug("Using SpeechKit endpoint: {}", properties.endpoint)
        logger.debug("Language: {}, Model: {}, AudioContainer: {}", properties.language, properties.model, properties.audioContainer)

        val audioBytes = try {
            val audioRequest = HttpRequest.newBuilder()
                .uri(URI.create(audioUrl))
                .GET()
                .build()
            val audioResponse = httpClient.send(audioRequest, HttpResponse.BodyHandlers.ofByteArray())
            if (audioResponse.statusCode() !in 200..299) {
                throw IllegalStateException("Failed to download audio from S3 presigned URL: HTTP ${audioResponse.statusCode()}")
            }
            audioResponse.body()
        } catch (ex: Exception) {
            logger.error("Failed to download audio from {}", audioUrl, ex)
            throw IllegalStateException("Failed to download audio file: ${ex.message}", ex)
        }
        val encodedAudio = java.util.Base64.getEncoder().encodeToString(audioBytes)

        val requestBody = mutableMapOf<String, Any>(
            "content" to encodedAudio,
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
        if (properties.folderId.isNotBlank() && properties.folderId != "folder_id") {
            requestBody["folderId"] = properties.folderId
        }

        val requestJson = objectMapper.writeValueAsString(requestBody)

        val startRawResponse = try {
            val requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(properties.endpoint))
                .header("Authorization", "Api-Key ${properties.apiKey}")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))

            if (properties.folderId.isNotBlank() && properties.folderId != "folder_id") {
                requestBuilder.header("x-folder-id", properties.folderId)
            }

            val response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() !in 200..299) {
                logger.error("SpeechKit API error (HTTP {}): {}", response.statusCode(), response.body())
                throw IllegalStateException("Failed to start speech recognition: ${response.statusCode()} - ${response.body()}")
            }

            response.body() ?: throw IllegalStateException("Empty SpeechKit response")
        } catch (ex: Exception) {
            logger.error("SpeechKit request failed for URL: {}", audioUrl, ex)
            throw IllegalStateException("Failed to start speech recognition via SpeechKit: ${ex.message}", ex)
        }

        val opNode = objectMapper.readTree(startRawResponse)
        val operationId = opNode.get("id")?.asText()
            ?: throw IllegalStateException("SpeechKit returned no operation ID: $startRawResponse")

        logger.debug("Started asynchronous recognition, Operation ID: {}", operationId)

        var done = false
        var attempts = 0
        while (!done && attempts < 60) {
            Thread.sleep(5000)
            attempts++
            try {
                val request = HttpRequest.newBuilder()
                    .uri(URI.create("https://operation.api.cloud.yandex.net/operations/$operationId"))
                    .header("Authorization", "Api-Key ${properties.apiKey}")
                    .GET()
                    .build()

                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
                
                if (response.statusCode() !in 200..299) {
                    throw IllegalStateException("Failed to poll operation status: HTTP ${response.statusCode()}")
                }

                val statusRaw = response.body() ?: throw IllegalStateException("Empty operation status")

                val statusNode = objectMapper.readTree(statusRaw)
                done = statusNode.get("done")?.asBoolean() ?: false

                val errorNode = statusNode.get("error")
                if (errorNode != null && !errorNode.isNull) {
                    throw IllegalStateException("SpeechKit async operation failed: $errorNode")
                }
            } catch (ex: Exception) {
                logger.error("Failed to poll operation status", ex)
                throw IllegalStateException("Failed to poll operation status: ${ex.message}", ex)
            }
        }

        if (!done) {
            throw IllegalStateException("SpeechKit operation timed out after 5 minutes")
        }

        logger.debug("Operation {} completed successfully", operationId)

        val rawResponse = try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://stt.api.cloud.yandex.net/stt/v3/getRecognition?operationId=$operationId"))
                .header("Authorization", "Api-Key ${properties.apiKey}")
                .GET()
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() !in 200..299) {
                throw IllegalStateException("Failed to get recognition results: HTTP ${response.statusCode()}")
            }
            
            response.body() ?: throw IllegalStateException("Empty getRecognition response")
        } catch (ex: Exception) {
            logger.error("Failed to get recognition results", ex)
            throw IllegalStateException("Failed to get recognition results: ${ex.message}", ex)
        }

        val words = mutableListOf<WordTiming>()

        rawResponse.lines()
            .filter { it.isNotBlank() }
            .forEach { line ->
                try {
                    val root = objectMapper.readTree(line)
                    collectWords(root, words)
                } catch (e: Exception) {
                    logger.warn("Failed to parse line from recognition results: {}", line, e)
                }
            }

        logger.debug("SpeechKit returned {} words from response", words.size)

        val uniqueWords = words.distinctBy { "${it.word}_${it.startMs}_${it.endMs}" }.sortedBy { it.startMs }

        if (uniqueWords.isEmpty()) {
            logger.warn("SpeechKit returned no word timings. Raw response: {}", rawResponse)
            throw IllegalStateException("SpeechKit returned no word timings")
        }

        return uniqueWords
    }

    private fun collectWords(node: JsonNode, result: MutableList<WordTiming>) {
        if (node.isObject) {
            if (node.has("alternatives") && node.get("alternatives").isArray) {
                val alternatives = node.get("alternatives")
                if (!alternatives.isEmpty) {
                    collectWords(alternatives.get(0), result)
                }
                return
            }

            if (node.has("words") && node.get("words").isArray) {
                node.get("words").forEach { wordNode ->
                    parseWord(wordNode)?.let(result::add)
                }
                return
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