package com.kille.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import kotlin.collections.emptyList

@ConfigurationProperties(prefix = "app.book-service")
data class BookServiceProperties(
    @DefaultValue("10")
    val maxBooks: Int = 10,

    @DefaultValue
    val forbiddenTitles: List<String> = emptyList(),

    @DefaultValue("5")
    val maxChaptersPerBook: Int = 5,

    @DefaultValue("1000")
    val maxChapterLength: Int = 1000
)
