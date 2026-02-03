package com.yourapp.demo

import com.yourapp.application.service.BookService
import com.yourapp.application.service.ChapterService
import com.yourapp.config.BookServiceProperties
import com.yourapp.domain.model.Book
import com.yourapp.domain.model.BookId
import com.yourapp.domain.model.Chapter
import com.yourapp.domain.model.ChapterIndex
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DemoRunner(
    private val bookService: BookService,
    private val chapterService: ChapterService,
    private val properties: BookServiceProperties
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(DemoRunner::class.java)

    override fun run(vararg args: String?) {
        logger.info("==========================================")
        logger.info("STARTING SERVICE DEMONSTRATION")
        logger.info("==========================================")

        printConfiguration()
        demonstrateBookService()
        demonstrateChapterService()
        demonstrateConfigurationValidation()

        logger.info("==========================================")
        logger.info("DEMONSTRATION COMPLETED SUCCESSFULLY")
        logger.info("==========================================")
    }

    private fun printConfiguration() {
        logger.info("")
        logger.info("CURRENT CONFIGURATION:")
        logger.info("   - Maximum books: ${properties.maxBooks}")
        logger.info("   - Maximum chapters per book: ${properties.maxChaptersPerBook}")
        logger.info("   - Maximum chapter length: ${properties.maxChapterLength}")
        logger.info("   - Forbidden titles (blacklist): ${properties.forbiddenTitles}")
        logger.info("")
    }

    private fun demonstrateBookService() {
        logger.info("==========================================")
        logger.info("DEMONSTRATING BookService")
        logger.info("==========================================")

        logger.info("")
        logger.info("1. Creating a book...")
        val chapter1 = Chapter.createWithText(
            bookId = BookId.generate(),
            index = ChapterIndex(0),
            title = "Introduction",
            text = "This is the text of the first chapter"
        )

        val book1 = Book.create(
            title = "War and Peace",
            author = "Leo Tolstoy",
            language = "en",
            coverUrl = "https://example.com/cover1.jpg",
            chapters = listOf(chapter1),
            audio = false,
            text = true
        )

        try {
            val createdBook = bookService.create(book1)
            logger.info("   SUCCESS: Book created successfully:")
            logger.info("      ID: ${createdBook.id.value}")
            logger.info("      Title: ${createdBook.title}")
            logger.info("      Author: ${createdBook.author}")
            logger.info("      Language: ${createdBook.language}")
            logger.info("      Chapter count: ${createdBook.chapterCount()}")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to create book: ${e.message}")
        }

        logger.info("")
        logger.info("2. Finding book by ID...")
        try {
            val foundBook = bookService.findById(book1.id)
            if (foundBook != null) {
                logger.info("   SUCCESS: Book found:")
                logger.info("      ID: ${foundBook.id.value}")
                logger.info("      Title: ${foundBook.title}")
            } else {
                logger.warn("   WARNING: Book not found")
            }
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to find book: ${e.message}")
        }

        logger.info("")
        logger.info("3. Creating a second book...")
        val chapter2 = Chapter.createWithText(
            bookId = BookId.generate(),
            index = ChapterIndex(0),
            title = "Chapter 1",
            text = "Content of chapter 1"
        )

        val book2 = Book.create(
            title = "Master and Margarita",
            author = "Mikhail Bulgakov",
            language = "en",
            coverUrl = "https://example.com/cover2.jpg",
            chapters = listOf(chapter2),
            audio = false,
            text = true
        )

        try {
            val createdBook2 = bookService.create(book2)
            logger.info("   SUCCESS: Second book created successfully:")
            logger.info("      ID: ${createdBook2.id.value}")
            logger.info("      Title: ${createdBook2.title}")
            logger.info("      Author: ${createdBook2.author}")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to create second book: ${e.message}")
        }

        logger.info("")
        logger.info("4. Getting all books...")
        try {
            val allBooks = bookService.findAll()
            logger.info("   SUCCESS: Found ${allBooks.size} books")
            allBooks.forEachIndexed { index, book ->
                logger.info("      ${index + 1}. ${book.title} (${book.author})")
            }
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to get all books: ${e.message}")
        }

        logger.info("")
        logger.info("5. Updating a book...")
        try {
            val updatedChapter = Chapter.createWithText(
                bookId = book1.id,
                index = ChapterIndex(0),
                title = "Introduction (updated)",
                text = "Updated text of the first chapter"
            )

            val updatedBook = Book.createWithId(
                id = book1.id,
                title = "War and Peace (updated)",
                author = "Leo Tolstoy",
                language = "en",
                coverUrl = "https://example.com/cover1_updated.jpg",
                chapters = listOf(updatedChapter),
                audio = false,
                text = true
            )

            bookService.update(updatedBook)
            logger.info("   SUCCESS: Book updated successfully:")
            logger.info("      New title: ${updatedBook.title}")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to update book: ${e.message}")
        }

        logger.info("")
        logger.info("6. Checking book existence...")
        try {
            val exists = bookService.exists(book1.id)
            logger.info("   SUCCESS: Book with ID ${book1.id.value} exists: $exists")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to check existence: ${e.message}")
        }

        logger.info("")
        logger.info("7. Counting total books...")
        try {
            val count = bookService.count()
            logger.info("   SUCCESS: Total books in system: $count")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to count books: ${e.message}")
        }

        logger.info("")
        logger.info("8. Deleting a book...")
        try {
            bookService.delete(book1.id)
            logger.info("   SUCCESS: Book deleted successfully: ${book1.title}")
            logger.info("   INFO: Remaining books: ${bookService.count()}")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to delete book: ${e.message}")
        }
    }

    private fun demonstrateChapterService() {
        logger.info("")
        logger.info("==========================================")
        logger.info("DEMONSTRATING ChapterService")
        logger.info("==========================================")

        val testBookId = BookId.generate()
        val testChapter = Chapter.createWithText(
            bookId = testBookId,
            index = ChapterIndex(0),
            title = "Test chapter",
            text = "Test chapter text"
        )

        val testBook = Book.create(
            title = "Test book for chapters",
            author = "Test author",
            language = "en",
            chapters = listOf(testChapter),
            audio = false,
            text = true
        )
        bookService.create(testBook)

        logger.info("")
        logger.info("1. Creating a chapter...")
        val chapter = Chapter.createWithText(
            bookId = testBookId,
            index = ChapterIndex(1),
            title = "Chapter 1: Beginning",
            text = "This is the content of the first chapter of the book"
        )

        try {
            val createdChapter = chapterService.create(chapter)
            logger.info("   SUCCESS: Chapter created successfully:")
            logger.info("      ID: ${createdChapter.id}")
            logger.info("      Title: ${createdChapter.title}")
            logger.info("      Index: ${createdChapter.index.value}")
            logger.info("      Text length: ${createdChapter.text?.length ?: 0} characters")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to create chapter: ${e.message}")
        }

        logger.info("")
        logger.info("2. Creating a second chapter...")
        val chapter2 = Chapter.createWithText(
            bookId = testBookId,
            index = ChapterIndex(2),
            title = "Chapter 2: Development",
            text = "This is the content of the second chapter of the book"
        )

        try {
            val createdChapter2 = chapterService.create(chapter2)
            logger.info("   SUCCESS: Second chapter created successfully:")
            logger.info("      Index: ${createdChapter2.index.value}")
            logger.info("      Title: ${createdChapter2.title}")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to create second chapter: ${e.message}")
        }

        logger.info("")
        logger.info("3. Finding chapter by ID...")
        try {
            val foundChapter = chapterService.findById(chapter.id)
            if (foundChapter != null) {
                logger.info("   SUCCESS: Chapter found:")
                logger.info("      Title: ${foundChapter.title}")
                logger.info("      Index: ${foundChapter.index.value}")
            } else {
                logger.warn("   WARNING: Chapter not found")
            }
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to find chapter: ${e.message}")
        }

        logger.info("")
        logger.info("4. Finding all chapters of the book...")
        try {
            val chaptersInBook = chapterService.findByBookId(testBookId)
            logger.info("   SUCCESS: Found ${chaptersInBook.size} chapters")
            chaptersInBook.forEach { ch ->
                logger.info("      - Chapter ${ch.index.value}: ${ch.title}")
            }
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to find book chapters: ${e.message}")
        }

        logger.info("")
        logger.info("5. Getting all chapters in system...")
        try {
            val allChapters = chapterService.findAll()
            logger.info("   SUCCESS: Total chapters in system: ${allChapters.size}")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to get all chapters: ${e.message}")
        }

        logger.info("")
        logger.info("6. Updating a chapter...")
        try {
            val updatedChapter = chapter.updateText("Updated content of the first chapter")
            chapterService.update(updatedChapter)
            logger.info("   SUCCESS: Chapter updated successfully")
            logger.info("      New text length: ${updatedChapter.text?.length ?: 0} characters")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to update chapter: ${e.message}")
        }

        logger.info("")
        logger.info("7. Checking chapter existence...")
        try {
            val exists = chapterService.exists(chapter.id)
            logger.info("   SUCCESS: Chapter exists: $exists")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to check existence: ${e.message}")
        }

        logger.info("")
        logger.info("8. Counting chapters for book...")
        try {
            val count = chapterService.countByBookId(testBookId)
            logger.info("   SUCCESS: Chapter count in book: $count")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to count chapters: ${e.message}")
        }

        logger.info("")
        logger.info("9. Deleting one chapter...")
        try {
            chapterService.delete(chapter.id)
            logger.info("   SUCCESS: Chapter deleted successfully")
            logger.info("   INFO: Remaining chapters for book: ${chapterService.countByBookId(testBookId)}")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to delete chapter: ${e.message}")
        }

        logger.info("")
        logger.info("10. Deleting all chapters of the book...")
        try {
            chapterService.deleteByBookId(testBookId)
            logger.info("   SUCCESS: All book chapters deleted successfully")
            logger.info("   INFO: Remaining chapters for book: ${chapterService.countByBookId(testBookId)}")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to delete book chapters: ${e.message}")
        }
    }

    private fun demonstrateConfigurationValidation() {
        logger.info("")
        logger.info("==========================================")
        logger.info("DEMONSTRATING CONFIGURATION VALIDATION")
        logger.info("==========================================")

        logger.info("")
        logger.info("1. Attempting to create book with forbidden title...")
        logger.info("   INFO: Forbidden titles: ${properties.forbiddenTitles}")

        val forbiddenTitle = properties.forbiddenTitles.firstOrNull() ?: "Test Book"
        val forbiddenChapter = Chapter.createWithText(
            bookId = BookId.generate(),
            index = ChapterIndex(0),
            title = "Chapter",
            text = "Text"
        )

        val forbiddenBook = Book.create(
            title = forbiddenTitle,
            author = "Test author",
            language = "en",
            chapters = listOf(forbiddenChapter),
            audio = false,
            text = true
        )

        try {
            bookService.create(forbiddenBook)
            logger.warn("   WARNING: Book was created (unexpected!)")
        } catch (e: IllegalArgumentException) {
            logger.info("   SUCCESS: CONFIGURATION IS WORKING!")
            logger.info("   REJECTED: Creation rejected: ${e.message}")
            logger.info("   INFO: Title '$forbiddenTitle' is in the blacklist")
        } catch (e: Exception) {
            logger.error("   ERROR: Unexpected error: ${e.message}")
        }

        logger.info("")
        logger.info("2. Checking maximum books limit...")
        logger.info("   INFO: Maximum books: ${properties.maxBooks}")
        logger.info("   INFO: Current count: ${bookService.count()}")

        if (bookService.count() < properties.maxBooks) {
            logger.info("   SUCCESS: Limit not reached, can create new books")
        } else {
            logger.info("   WARNING: Maximum book limit reached")
        }

        logger.info("")
        logger.info("3. Attempting to create chapter exceeding maximum length...")
        logger.info("   INFO: Maximum chapter length: ${properties.maxChapterLength} characters")

        val longText = "a".repeat(properties.maxChapterLength + 100)
        val bookForLongChapter = BookId.generate()

        val tempChapter = Chapter.createWithText(
            bookId = bookForLongChapter,
            index = ChapterIndex(0),
            title = "Temp",
            text = "temp"
        )
        val tempBook = Book.create(
            title = "Temporary book",
            author = "Author",
            language = "en",
            chapters = listOf(tempChapter),
            audio = false,
            text = true
        )
        bookService.create(tempBook)

        val longChapter = Chapter.createWithText(
            bookId = bookForLongChapter,
            index = ChapterIndex(1),
            title = "Too long chapter",
            text = longText
        )

        try {
            chapterService.create(longChapter)
            logger.warn("   WARNING: Chapter was created (unexpected!)")
        } catch (e: IllegalArgumentException) {
            logger.info("   SUCCESS: CONFIGURATION IS WORKING!")
            logger.info("   REJECTED: Creation rejected: ${e.message}")
            logger.info("   INFO: Text length ${longText.length} exceeds maximum ${properties.maxChapterLength}")
        } catch (e: Exception) {
            logger.error("   ERROR: Unexpected error: ${e.message}")
        }

        logger.info("")
        logger.info("4. Checking chapters per book limit...")
        logger.info("   INFO: Maximum chapters per book: ${properties.maxChaptersPerBook}")

        val bookForChapters = BookId.generate()
        val initialChapter = Chapter.createWithText(
            bookId = bookForChapters,
            index = ChapterIndex(0),
            title = "Start",
            text = "text"
        )
        val bookForManyChapters = Book.create(
            title = "Book with chapters",
            author = "Author",
            language = "en",
            chapters = listOf(initialChapter),
            audio = false,
            text = true
        )
        bookService.create(bookForManyChapters)

        try {
            for (i in 1 until properties.maxChaptersPerBook) {
                val ch = Chapter.createWithText(
                    bookId = bookForChapters,
                    index = ChapterIndex(i),
                    title = "Chapter $i",
                    text = "Content of chapter $i"
                )
                chapterService.create(ch)
            }

            val currentCount = chapterService.countByBookId(bookForChapters)
            logger.info("   SUCCESS: Created $currentCount chapters")

            if (currentCount >= properties.maxChaptersPerBook) {
                val extraChapter = Chapter.createWithText(
                    bookId = bookForChapters,
                    index = ChapterIndex(properties.maxChaptersPerBook),
                    title = "Extra chapter",
                    text = "Text of extra chapter"
                )

                try {
                    chapterService.create(extraChapter)
                    logger.warn("   WARNING: Chapter was created (unexpected!)")
                } catch (e: IllegalStateException) {
                    logger.info("   SUCCESS: CONFIGURATION IS WORKING!")
                    logger.info("   REJECTED: Creation rejected: ${e.message}")
                    logger.info("   INFO: Reached chapters per book limit: ${properties.maxChaptersPerBook}")
                }
            }
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to check chapters limit: ${e.message}")
        }

        logger.info("")
        logger.info("Cleaning up demonstration data...")
        try {
            bookService.clear()
            chapterService.clear()
            logger.info("   SUCCESS: Data cleared")
        } catch (e: Exception) {
            logger.error("   ERROR: Failed to clear: ${e.message}")
        }
    }
}
