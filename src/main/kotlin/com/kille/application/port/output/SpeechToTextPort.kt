package com.kille.application.port.output

data class WordTiming(
    val word: String,
    val startMs: Long,
    val endMs: Long
)

interface SpeechToTextPort {
    fun recognizeWords(audioUrl: String): List<WordTiming>
}

