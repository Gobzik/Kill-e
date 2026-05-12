package com.kille.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
class HttpClientConfig {

    @Bean
    fun restClientBuilder(): RestClient.Builder {
        val factory = JdkClientHttpRequestFactory()
        factory.setReadTimeout(30000)
        return RestClient.builder().requestFactory(factory)
    }

    @Bean
    fun restClient(builder: RestClient.Builder): RestClient {
        return builder.build()
    }
}


