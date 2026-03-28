package com.kille.infrastructure.storage

import com.kille.config.S3Properties
import com.kille.infrastructure.storage.service.S3StorageService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.mock.web.MockMultipartFile
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.http.AbortableInputStream
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.model.S3Object
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest
import java.io.ByteArrayInputStream
import java.net.URL
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class S3StorageServiceTest {

    @Mock
    private lateinit var s3Client: S3Client

    @Mock
    private lateinit var s3Presigner: S3Presigner

    private lateinit var service: S3StorageService

    private val bookId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val chapterId: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")
    private val expectedKeyPrefix = "books/$bookId/$chapterId"

    private val properties = S3Properties(
        endpoint = "https://storage.yandexcloud.net",
        region = "ru-central1",
        accessKey = "test-key",
        secretKey = "test-secret",
        bucket = "test-bucket",
        chaptersPrefix = "books",
        pathStyleAccessEnabled = true,
        presignedUrlExpirationHours = 1
    )

    @BeforeEach
    fun setUp() {
        service = S3StorageService(s3Client, s3Presigner, properties)
    }

    // --- Key generation ---

    @Test
    fun `uploadAudio generates key with correct path format`() {
        `when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java)))
            .thenReturn(PutObjectResponse.builder().build())
        val file = MockMultipartFile("audio", "audio.mp3", "audio/mpeg", byteArrayOf(1, 2, 3))

        val key = service.uploadAudio(bookId, chapterId, file)

        assertEquals("$expectedKeyPrefix/audio.mp3", key)
    }

    @Test
    fun `uploadText generates key with correct path format`() {
        `when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java)))
            .thenReturn(PutObjectResponse.builder().build())

        val key = service.uploadText(bookId, chapterId, "content")

        assertEquals("$expectedKeyPrefix/text.txt", key)
    }

    @Test
    fun `uploadTimings generates key with correct path format`() {
        `when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java)))
            .thenReturn(PutObjectResponse.builder().build())

        val key = service.uploadTimings(bookId, chapterId, """{"timings":[]}""")

        assertEquals("$expectedKeyPrefix/timings.json", key)
    }

    @Test
    fun `chaptersPrefix with leading and trailing slashes is normalized`() {
        val serviceWithSlashes = S3StorageService(
            s3Client, s3Presigner, properties.copy(chaptersPrefix = "/books/")
        )
        `when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java)))
            .thenReturn(PutObjectResponse.builder().build())

        val key = serviceWithSlashes.uploadText(bookId, chapterId, "content")

        assertEquals("books/$bookId/$chapterId/text.txt", key)
    }

    // --- uploadAudio ---

    @Test
    fun `uploadAudio calls putObject and returns the key`() {
        val file = MockMultipartFile("audio", "audio.mp3", "audio/mpeg", byteArrayOf(1, 2, 3))
        `when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java)))
            .thenReturn(PutObjectResponse.builder().build())

        val key = service.uploadAudio(bookId, chapterId, file)

        verify(s3Client).putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java))
        assertTrue(key.endsWith("audio.mp3"))
    }

    @Test
    fun `uploadAudio wraps SDK exception in RuntimeException`() {
        val file = MockMultipartFile("audio", "audio.mp3", "audio/mpeg", byteArrayOf(1))
        `when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java)))
            .thenThrow(SdkClientException.create("upload failed"))

        assertThrows(RuntimeException::class.java) {
            service.uploadAudio(bookId, chapterId, file)
        }
    }

    // --- uploadText ---

    @Test
    fun `uploadText calls putObject with plain-text content type`() {
        `when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java)))
            .thenReturn(PutObjectResponse.builder().build())

        service.uploadText(bookId, chapterId, "Chapter text")

        verify(s3Client).putObject(
            argThat<PutObjectRequest> { it.contentType() == "text/plain" },
            any(RequestBody::class.java)
        )
    }

    @Test
    fun `uploadText wraps SDK exception in RuntimeException`() {
        `when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java)))
            .thenThrow(SdkClientException.create("upload failed"))

        assertThrows(RuntimeException::class.java) {
            service.uploadText(bookId, chapterId, "text")
        }
    }

    // --- uploadTimings ---

    @Test
    fun `uploadTimings calls putObject with JSON content type`() {
        `when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java)))
            .thenReturn(PutObjectResponse.builder().build())

        service.uploadTimings(bookId, chapterId, """{"timings":[]}""")

        verify(s3Client).putObject(
            argThat<PutObjectRequest> { it.contentType() == "application/json" },
            any(RequestBody::class.java)
        )
    }

    // --- getAudioUrl ---

    @Test
    fun `getAudioUrl returns presigned URL when audio file exists`() {
        val expectedUrl = "https://test-bucket.example.com/audio.mp3?X-Amz-Signature=abc"
        val presignedRequest = mock(PresignedGetObjectRequest::class.java)
        `when`(presignedRequest.url()).thenReturn(URL(expectedUrl))
        `when`(s3Client.headObject(any(HeadObjectRequest::class.java)))
            .thenReturn(HeadObjectResponse.builder().build())
        `when`(s3Presigner.presignGetObject(any(GetObjectPresignRequest::class.java)))
            .thenReturn(presignedRequest)

        val url = service.getAudioUrl(bookId, chapterId)

        assertEquals(expectedUrl, url)
    }

    @Test
    fun `getAudioUrl returns null when audio file does not exist`() {
        `when`(s3Client.headObject(any(HeadObjectRequest::class.java)))
            .thenThrow(NoSuchKeyException.builder().build())

        val url = service.getAudioUrl(bookId, chapterId)

        assertNull(url)
        verify(s3Presigner, never()).presignGetObject(any(GetObjectPresignRequest::class.java))
    }

    @Test
    fun `getAudioUrl returns null when S3 exception occurs`() {
        `when`(s3Client.headObject(any(HeadObjectRequest::class.java)))
            .thenThrow(S3Exception.builder().message("access denied").statusCode(403).build())

        val url = service.getAudioUrl(bookId, chapterId)

        assertNull(url)
    }

    // --- getText ---

    @Test
    fun `getText returns content when text file exists`() {
        val content = "Chapter text content"
        val responseInputStream = ResponseInputStream(
            GetObjectResponse.builder().build(),
            AbortableInputStream.create(ByteArrayInputStream(content.toByteArray()))
        )
        `when`(s3Client.headObject(any(HeadObjectRequest::class.java)))
            .thenReturn(HeadObjectResponse.builder().build())
        `when`(s3Client.getObject(any(GetObjectRequest::class.java)))
            .thenReturn(responseInputStream)

        val result = service.getText(bookId, chapterId)

        assertEquals(content, result)
    }

    @Test
    fun `getText returns null when text file does not exist`() {
        `when`(s3Client.headObject(any(HeadObjectRequest::class.java)))
            .thenThrow(NoSuchKeyException.builder().build())

        val result = service.getText(bookId, chapterId)

        assertNull(result)
        verify(s3Client, never()).getObject(any(GetObjectRequest::class.java))
    }

    @Test
    fun `getText returns null when S3 exception occurs`() {
        `when`(s3Client.headObject(any(HeadObjectRequest::class.java)))
            .thenThrow(S3Exception.builder().message("error").statusCode(500).build())

        val result = service.getText(bookId, chapterId)

        assertNull(result)
    }

    // --- getTimings ---

    @Test
    fun `getTimings returns content when timings file exists`() {
        val timings = """{"timings":[{"word":"hello","start":0.0}]}"""
        val responseInputStream = ResponseInputStream(
            GetObjectResponse.builder().build(),
            AbortableInputStream.create(ByteArrayInputStream(timings.toByteArray()))
        )
        `when`(s3Client.headObject(any(HeadObjectRequest::class.java)))
            .thenReturn(HeadObjectResponse.builder().build())
        `when`(s3Client.getObject(any(GetObjectRequest::class.java)))
            .thenReturn(responseInputStream)

        val result = service.getTimings(bookId, chapterId)

        assertEquals(timings, result)
    }

    @Test
    fun `getTimings returns null when timings file does not exist`() {
        `when`(s3Client.headObject(any(HeadObjectRequest::class.java)))
            .thenThrow(NoSuchKeyException.builder().build())

        val result = service.getTimings(bookId, chapterId)

        assertNull(result)
    }

    // --- deleteChapterFiles ---

    @Test
    fun `deleteChapterFiles calls deleteObjects when files exist`() {
        val s3Object = S3Object.builder().key("$expectedKeyPrefix/audio.mp3").build()
        `when`(s3Client.listObjectsV2(any(ListObjectsV2Request::class.java)))
            .thenReturn(ListObjectsV2Response.builder().contents(listOf(s3Object)).build())
        `when`(s3Client.deleteObjects(any(DeleteObjectsRequest::class.java)))
            .thenReturn(DeleteObjectsResponse.builder().build())

        service.deleteChapterFiles(bookId, chapterId)

        verify(s3Client).deleteObjects(any(DeleteObjectsRequest::class.java))
    }

    @Test
    fun `deleteChapterFiles skips deleteObjects when no files exist`() {
        `when`(s3Client.listObjectsV2(any(ListObjectsV2Request::class.java)))
            .thenReturn(ListObjectsV2Response.builder().contents(emptyList()).build())

        service.deleteChapterFiles(bookId, chapterId)

        verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest::class.java))
    }

    @Test
    fun `deleteChapterFiles wraps S3 exception in RuntimeException`() {
        `when`(s3Client.listObjectsV2(any(ListObjectsV2Request::class.java)))
            .thenThrow(S3Exception.builder().message("error").statusCode(500).build())

        assertThrows(RuntimeException::class.java) {
            service.deleteChapterFiles(bookId, chapterId)
        }
    }

    // --- chapterExists ---

    @Test
    fun `chapterExists returns true when chapter files are found`() {
        val s3Object = S3Object.builder().key("$expectedKeyPrefix/audio.mp3").build()
        `when`(s3Client.listObjectsV2(any(ListObjectsV2Request::class.java)))
            .thenReturn(ListObjectsV2Response.builder().contents(listOf(s3Object)).build())

        assertTrue(service.chapterExists(bookId, chapterId))
    }

    @Test
    fun `chapterExists returns false when no chapter files are found`() {
        `when`(s3Client.listObjectsV2(any(ListObjectsV2Request::class.java)))
            .thenReturn(ListObjectsV2Response.builder().contents(emptyList()).build())

        assertFalse(service.chapterExists(bookId, chapterId))
    }

    @Test
    fun `chapterExists returns false when S3 exception occurs`() {
        `when`(s3Client.listObjectsV2(any(ListObjectsV2Request::class.java)))
            .thenThrow(S3Exception.builder().message("error").statusCode(500).build())

        assertFalse(service.chapterExists(bookId, chapterId))
    }

    // --- getAudioInputStream ---

    @Test
    fun `getAudioInputStream returns stream when audio file exists`() {
        val responseInputStream = ResponseInputStream(
            GetObjectResponse.builder().build(),
            AbortableInputStream.create(ByteArrayInputStream(byteArrayOf(1, 2, 3)))
        )
        `when`(s3Client.headObject(any(HeadObjectRequest::class.java)))
            .thenReturn(HeadObjectResponse.builder().build())
        `when`(s3Client.getObject(any(GetObjectRequest::class.java)))
            .thenReturn(responseInputStream)

        val result = service.getAudioInputStream(bookId, chapterId)

        assertNotNull(result)
    }

    @Test
    fun `getAudioInputStream returns null when audio file does not exist`() {
        `when`(s3Client.headObject(any(HeadObjectRequest::class.java)))
            .thenThrow(NoSuchKeyException.builder().build())

        val result = service.getAudioInputStream(bookId, chapterId)

        assertNull(result)
        verify(s3Client, never()).getObject(any(GetObjectRequest::class.java))
    }

    // --- uploadChapterFiles ---

    @Test
    fun `uploadChapterFiles uploads all provided files and returns their keys`() {
        val file = MockMultipartFile("audio", "audio.mp3", "audio/mpeg", byteArrayOf(1, 2, 3))
        `when`(s3Client.putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java)))
            .thenReturn(PutObjectResponse.builder().build())

        val result = service.uploadChapterFiles(bookId, chapterId, file, "text content", """{}""")

        assertEquals("$expectedKeyPrefix/audio.mp3", result.audioKey)
        assertEquals("$expectedKeyPrefix/text.txt", result.textKey)
        assertEquals("$expectedKeyPrefix/timings.json", result.timingsKey)
        verify(s3Client, times(3)).putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java))
    }

    @Test
    fun `uploadChapterFiles handles all-null inputs without calling S3`() {
        val result = service.uploadChapterFiles(bookId, chapterId, null, null, null)

        assertNull(result.audioKey)
        assertNull(result.textKey)
        assertNull(result.timingsKey)
        verify(s3Client, never()).putObject(any(PutObjectRequest::class.java), any(RequestBody::class.java))
    }

    // --- getChapterFiles ---

    @Test
    fun `getChapterFiles returns all available file content`() {
        val textContent = "Hello"
        val timingsContent = """{"timings":[]}"""
        val expectedUrl = "https://test.example.com/audio.mp3?signed=true"
        val presignedRequest = mock(PresignedGetObjectRequest::class.java)
        `when`(presignedRequest.url()).thenReturn(URL(expectedUrl))
        `when`(s3Client.headObject(any(HeadObjectRequest::class.java)))
            .thenReturn(HeadObjectResponse.builder().build())
        `when`(s3Presigner.presignGetObject(any(GetObjectPresignRequest::class.java)))
            .thenReturn(presignedRequest)
        `when`(s3Client.getObject(any(GetObjectRequest::class.java)))
            .thenReturn(
                ResponseInputStream(
                    GetObjectResponse.builder().build(),
                    AbortableInputStream.create(ByteArrayInputStream(textContent.toByteArray()))
                ),
                ResponseInputStream(
                    GetObjectResponse.builder().build(),
                    AbortableInputStream.create(ByteArrayInputStream(timingsContent.toByteArray()))
                )
            )

        val result = service.getChapterFiles(bookId, chapterId)

        assertEquals(expectedUrl, result.audioUrl)
        assertEquals(textContent, result.textContent)
        assertEquals(timingsContent, result.timingsContent)
    }
}
