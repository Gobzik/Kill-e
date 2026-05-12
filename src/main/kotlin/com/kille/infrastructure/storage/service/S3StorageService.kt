package com.kille.infrastructure.storage.service

import com.kille.application.port.output.StoragePort
import com.kille.config.S3Properties
import com.kille.infrastructure.storage.exception.StorageOperationException
import com.kille.infrastructure.storage.dto.ChapterFiles
import com.kille.infrastructure.storage.dto.FileUploadResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.model.S3Object
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.io.InputStream
import java.time.Duration
import java.util.UUID

@Service
class S3StorageService(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    private val properties: S3Properties
) : StoragePort {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val AUDIO_FILE = "audio.mp3"
        private const val TEXT_FILE = "text.txt"
        private const val TIMINGS_FILE = "timings.json"
    }

    override fun uploadAudio(
        bookId: UUID,
        chapterId: UUID,
        file: MultipartFile
    ): String {
        val key = generateChapterKey(bookId, chapterId, AUDIO_FILE)
        return uploadFile(key, file)
    }

    override fun uploadText(bookId: UUID, chapterId: UUID, content: String): String {
        val key = generateChapterKey(bookId, chapterId, TEXT_FILE)
        return uploadContent(key, content, "text/plain")
    }

    override fun uploadTimings(bookId: UUID, chapterId: UUID, timings: String): String {
        val key = generateChapterKey(bookId, chapterId, TIMINGS_FILE)
        return uploadContent(key, timings, "application/json")
    }

    override fun getAudioUrl(bookId: UUID, chapterId: UUID): String? {
        val key = generateChapterKey(bookId, chapterId, AUDIO_FILE)
        return try {
            if (fileExists(key)) {
                generatePresignedUrl(key)
            } else {
                null
            }
        } catch (e: S3Exception) {
            logger.error("Error getting audio URL for book $bookId, chapter $chapterId", e)
            null
        }
    }

    override fun getText(bookId: UUID, chapterId: UUID): String? {
        val key = generateChapterKey(bookId, chapterId, TEXT_FILE)
        return try {
            getFileContent(key)
        } catch (e: S3Exception) {
            logger.error("Error getting text for book $bookId, chapter $chapterId", e)
            null
        }
    }

    override fun getTimings(bookId: UUID, chapterId: UUID): String? {
        val key = generateChapterKey(bookId, chapterId, TIMINGS_FILE)
        return try {
            getFileContent(key)
        } catch (e: S3Exception) {
            logger.error("Error getting timings for book $bookId, chapter $chapterId", e)
            null
        }
    }

    override fun deleteChapterFiles(bookId: UUID, chapterId: UUID) {
        val prefix = chapterPrefix(bookId, chapterId)
        try {
            val objectsToDelete = listObjects(prefix)

            if (objectsToDelete.isNotEmpty()) {
                val deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(properties.bucket)
                    .delete(
                        Delete.builder()
                            .objects(objectsToDelete.map { ObjectIdentifier.builder().key(it.key()).build() })
                            .build()
                    )
                    .build()

                s3Client.deleteObjects(deleteRequest)
                logger.info("Deleted files for book $bookId, chapter $chapterId")
            }
        } catch (e: S3Exception) {
            logger.error("Error deleting files for book $bookId, chapter $chapterId", e)
            throw StorageOperationException("Failed to delete chapter files: ${e.message}", e)
        }
    }

    override fun chapterExists(bookId: UUID, chapterId: UUID): Boolean {
        val prefix = chapterPrefix(bookId, chapterId)
        return try {
            listObjects(prefix).isNotEmpty()
        } catch (e: S3Exception) {
            logger.error("Error checking chapter existence", e)
            false
        }
    }

    override fun getAudioInputStream(bookId: UUID, chapterId: UUID): InputStream? {
        val key = generateChapterKey(bookId, chapterId, AUDIO_FILE)
        return try {
            if (fileExists(key)) {
                val request = GetObjectRequest.builder()
                    .bucket(properties.bucket)
                    .key(key)
                    .build()
                s3Client.getObject(request)
            } else {
                null
            }
        } catch (e: S3Exception) {
            logger.error("Error getting audio stream for book $bookId, chapter $chapterId", e)
            null
        }
    }

    fun uploadChapterFiles(
        bookId: UUID,
        chapterId: UUID,
        audioFile: MultipartFile? = null,
        textContent: String? = null,
        timingsContent: String? = null
    ): FileUploadResult {
        val audioKey = audioFile?.let { uploadAudio(bookId, chapterId, it) }
        val textKey = textContent?.let { uploadText(bookId, chapterId, it) }
        val timingsKey = timingsContent?.let { uploadTimings(bookId, chapterId, it) }

        return FileUploadResult(
            audioKey = audioKey,
            textKey = textKey,
            timingsKey = timingsKey
        )
    }

    fun getChapterFiles(bookId: UUID, chapterId: UUID): ChapterFiles {
        return ChapterFiles(
            audioUrl = getAudioUrl(bookId, chapterId),
            textContent = getText(bookId, chapterId),
            timingsContent = getTimings(bookId, chapterId)
        )
    }

    private fun generateChapterKey(bookId: UUID, chapterId: UUID, filename: String): String {
        return "${normalizedPrefix()}/${bookId}/${chapterId}/$filename"
    }

    private fun chapterPrefix(bookId: UUID, chapterId: UUID): String {
        return "${normalizedPrefix()}/${bookId}/${chapterId}/"
    }

    private fun normalizedPrefix(): String {
        return properties.chaptersPrefix.trim().trim('/')
    }

    override fun uploadFile(key: String, file: MultipartFile): String {
        try {
            val request = PutObjectRequest.builder()
                .bucket(properties.bucket)
                .key(key)
                .contentType(file.contentType ?: "application/octet-stream")
                .contentLength(file.size)
                .build()

            file.inputStream.use { inputStream ->
                s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(inputStream, file.size)
                )
            }

            logger.info("File uploaded successfully: $key")
            return key
        } catch (e: SdkException) {
            logger.error("Failed to upload file: $key", e)
            throw StorageOperationException("Failed to upload file to S3 storage: ${e.message}", e)
        }
    }

    override fun uploadContent(key: String, content: String, contentType: String): String {
        try {
            val request = PutObjectRequest.builder()
                .bucket(properties.bucket)
                .key(key)
                .contentType(contentType)
                .build()

            s3Client.putObject(
                request,
                RequestBody.fromString(content)
            )

            logger.info("Content uploaded successfully: $key")
            return key
        } catch (e: SdkException) {
            logger.error("Failed to upload content: $key", e)
            throw StorageOperationException("Failed to upload content to S3 storage: ${e.message}", e)
        }
    }

    override fun getPresignedUrl(key: String): String {
        return generatePresignedUrl(key)
    }

    override fun getFileContent(key: String): String? {
        try {
            if (!fileExists(key)) {
                return null
            }

            val request = GetObjectRequest.builder()
                .bucket(properties.bucket)
                .key(key)
                .build()

            val response = s3Client.getObject(request)
            return response.use { inputStream ->
                inputStream.bufferedReader().readText()
            }
        } catch (e: S3Exception) {
            if (e.statusCode() == 404) {
                return null
            }
            throw e
        }
    }

    private fun fileExists(key: String): Boolean {
        try {
            s3Client.headObject(
                HeadObjectRequest.builder()
                    .bucket(properties.bucket)
                    .key(key)
                    .build()
            )
            return true
        } catch (_: NoSuchKeyException) {
            return false
        } catch (e: S3Exception) {
            if (e.statusCode() == 404) {
                return false
            }
            throw e
        } catch (e: SdkException) {
            logger.error("Failed to check file existence: $key", e)
            return false
        }
    }

    private fun generatePresignedUrl(key: String): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(properties.bucket)
            .key(key)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofHours(properties.presignedUrlExpirationHours))
            .getObjectRequest(getObjectRequest)
            .build()

        return s3Presigner.presignGetObject(presignRequest).url().toString()
    }

    private fun listObjects(prefix: String): List<S3Object> {
        val request = ListObjectsV2Request.builder()
            .bucket(properties.bucket)
            .prefix(prefix)
            .build()

        return s3Client.listObjectsV2(request).contents()
    }
}
