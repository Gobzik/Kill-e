package com.kille.application.port.output

/**
 * Cross-provider normalized representation of word timing.
 */
data class WordTiming(
    val word: String,
    val startMs: Long,
    val endMs: Long
)

interface SpeechToTextPort {
    fun recognizeWords(audioUrl: String): List<WordTiming>
}

