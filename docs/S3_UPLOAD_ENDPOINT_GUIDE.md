# S3 upload endpoint guide

Этот документ для разработчика, который будет добавлять endpoint загрузки файлов главы.

## Что уже сделано

- Добавлен сервис `S3StorageService` (`src/main/kotlin/com/kille/infrastructure/storage/service/S3StorageService.kt`).
- Сервис умеет:
  - загружать аудио/текст/тайминги;
  - получать данные и presigned URL аудио;
  - удалять все файлы главы;
  - проверять, есть ли файлы главы.
- Подключен S3-конфиг для Yandex Object Storage (`src/main/kotlin/com/kille/infrastructure/storage/config/S3Config.kt`).
- Описаны переменные окружения (`docs/S3_VARIABLES.md`).

## Структура объектов в S3

Хранение организовано по ключам:

- `{chaptersPrefix}/{bookId}/{chapterId}/audio.mp3`
- `{chaptersPrefix}/{bookId}/{chapterId}/text.txt`
- `{chaptersPrefix}/{bookId}/{chapterId}/timings.json`

По умолчанию `chaptersPrefix=books`, то есть итог:

- `books/{bookId}/{chapterId}/audio.mp3`
- `books/{bookId}/{chapterId}/text.txt`
- `books/{bookId}/{chapterId}/timings.json`

## Как подключить endpoint загрузки

1. В `ChapterController` добавьте новый маршрут, например:
   - `POST /api/v1/chapters/{chapterId}/files`
2. В методе принимайте:
   - `bookId` (как query/path param),
   - `audio` (`MultipartFile?`),
   - `text` (`String?`),
   - `timings` (`String?`).
3. Инжектируйте `S3StorageService` в контроллер или use-case.
4. Вызовите `uploadChapterFiles(bookId, chapterId, audio, text, timings)`.
5. Сохраните возвращенные ключи (`audioKey`, `textKey`, `timingsKey`) в полях главы:
   - `audioUrl` <- `audioKey`,
   - `timingUrl` <- `timingsKey`,
   - текст можно хранить либо в БД (`text`), либо как `textKey` в отдельном поле (по вашей модели).
6. Для чтения:
   - presigned URL аудио: `getAudioUrl(bookId, chapterId)`;
   - текст: `getText(bookId, chapterId)`;
   - тайминги: `getTimings(bookId, chapterId)`.

## Важные замечания

- Сейчас endpoint намеренно **не** добавлен (по требованию).
- Если хотите работать через абстракцию, можно использовать `StoragePort`.
- Для production желательно:
  - ограничить размер файла и тип контента;
  - добавить антивирус/валидацию;
  - логировать загрузки с correlation-id;
  - хранить в БД только ключи, а не публичные URL.

