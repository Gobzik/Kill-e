package com.kille.infrastructure.storage.config

import com.kille.config.S3Properties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
class S3Config(
    private val properties: S3Properties
) {

    @Bean
    fun s3Client(): S3Client {
        val creds = AwsBasicCredentials.create(properties.accessKey, properties.secretKey)
        val builder = S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(creds))
            .region(Region.of(properties.region))

        if (!properties.endpoint.isBlank()) {
            builder.endpointOverride(URI.create(properties.endpoint))
        }

        return builder.build()
    }

    @Bean
    fun s3Presigner(): S3Presigner {
        val creds = AwsBasicCredentials.create(properties.accessKey, properties.secretKey)
        val builder = S3Presigner.builder()
            .credentialsProvider(StaticCredentialsProvider.create(creds))
            .region(Region.of(properties.region))

        if (!properties.endpoint.isBlank()) {
            builder.endpointOverride(URI.create(properties.endpoint))
        }

        return builder.build()
    }
}

