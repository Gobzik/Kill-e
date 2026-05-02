package com.kille.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient

@Configuration
class HttpClientConfig {

    @Bean
    fun restClientBuilder(): RestClient.Builder {
        // Use JdkClientHttpRequestFactory to avoid any gRPC-related auto-configurations
        val factory = JdkClientHttpRequestFactory()
        factory.setReadTimeout(30000) // 30 seconds
        return RestClient.builder().requestFactory(factory)
    }

    @Bean
    fun restClient(builder: RestClient.Builder): RestClient {
        return builder.build()
    }
}


