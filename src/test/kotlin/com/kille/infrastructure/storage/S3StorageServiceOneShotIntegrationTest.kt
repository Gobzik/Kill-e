package com.kille.infrastructure.storage

import com.kille.config.S3Properties
import com.kille.infrastructure.storage.service.S3StorageService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI
import java.time.Instant
import java.util.UUID

class S3StorageServiceOneShotIntegrationTest {

    companion object {
        private val FIXED_BOOK_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000111")
        private val FIXED_CHAPTER_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000222")
    }

    @Test
    fun uploadText_overwritesSameObjectKeyEveryRun() {
        val runFlag = System.getenv("RUN_S3_ONE_SHOT_TEST")
        assumeTrue(
            runFlag.equals("true", ignoreCase = true),
            "Set RUN_S3_ONE_SHOT_TEST=true to run this manual one-shot integration test"
        )

        val accessKey = System.getenv("YANDEX_CLOUD_ACCESS_KEY")
        val secretKey = System.getenv("YANDEX_CLOUD_SECRET_KEY")
        val bucket = System.getenv("YANDEX_CLOUD_BUCKET")

        assumeTrue(!accessKey.isNullOrBlank(), "YANDEX_CLOUD_ACCESS_KEY is required")
        assumeTrue(!secretKey.isNullOrBlank(), "YANDEX_CLOUD_SECRET_KEY is required")
        assumeTrue(!bucket.isNullOrBlank(), "YANDEX_CLOUD_BUCKET is required")

        val endpoint = System.getenv("YANDEX_CLOUD_ENDPOINT") ?: "https://storage.yandexcloud.net"
        val region = System.getenv("YANDEX_CLOUD_REGION") ?: "ru-central1"
        val prefix = (System.getenv("YANDEX_CLOUD_CHAPTERS_PREFIX") ?: "books").ifBlank { "books" }
        val pathStyle = (System.getenv("YANDEX_CLOUD_PATH_STYLE_ACCESS_ENABLED") ?: "true").toBoolean()

        val properties = S3Properties(
            endpoint = endpoint,
            region = region,
            accessKey = accessKey!!,
            secretKey = secretKey!!,
            bucket = bucket!!,
            chaptersPrefix = prefix,
            pathStyleAccessEnabled = pathStyle,
            presignedUrlExpirationHours = 1
        )

        val creds = AwsBasicCredentials.create(properties.accessKey, properties.secretKey)
        val s3Configuration = S3Configuration.builder()
            .pathStyleAccessEnabled(properties.pathStyleAccessEnabled)
            .build()

        S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(creds))
            .region(Region.of(properties.region))
            .serviceConfiguration(s3Configuration)
            .endpointOverride(URI.create(properties.endpoint))
            .build()
            .use { s3Client ->
                S3Presigner.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(creds))
                    .region(Region.of(properties.region))
                    .serviceConfiguration(s3Configuration)
                    .endpointOverride(URI.create(properties.endpoint))
                    .build()
                    .use { s3Presigner ->
                        val service = S3StorageService(s3Client, s3Presigner, properties)
                        val content = "one-shot overwrite marker @ ${Instant.now()}"

                        val key = service.uploadText(FIXED_BOOK_ID, FIXED_CHAPTER_ID, content)
                        val loadedText = service.getText(FIXED_BOOK_ID, FIXED_CHAPTER_ID)

                        val head = s3Client.headObject(
                            HeadObjectRequest.builder()
                                .bucket(properties.bucket)
                                .key(key)
                                .build()
                        )
                        val listed = s3Client.listObjectsV2(
                            ListObjectsV2Request.builder()
                                .bucket(properties.bucket)
                                .prefix(key)
                                .maxKeys(1)
                                .build()
                        )

                        assertEquals(content, loadedText)
                        println("S3 one-shot write success")
                        println("Endpoint: ${properties.endpoint}")
                        println("Bucket: ${properties.bucket}")
                        println("Object key: $key")
                        println("ETag: ${head.eTag()}")
                        println("Size: ${head.contentLength()}")
                        println("Visible in LIST by exact prefix: ${listed.contents().isNotEmpty()}")
                    }
            }
    }
}


