# S3/Yandex Object Storage variables

Ниже список переменных, которые нужны приложению для работы с S3-совместимым хранилищем.

## Где задавать

- Для локального запуска: в корневом файле `.env` (можно скопировать из `.env.example`).
- Для Docker Compose: те же переменные читаются из `.env` и пробрасываются в сервис `app` в `compose.yaml`.
- Для CI/CD: задайте их как секреты/переменные окружения пайплайна.

## Обязательные переменные

- `YANDEX_CLOUD_ACCESS_KEY` - Access Key сервисного аккаунта.
- `YANDEX_CLOUD_SECRET_KEY` - Secret Key сервисного аккаунта.
- `YANDEX_CLOUD_BUCKET` - имя бакета, где будут храниться файлы глав.

## Опциональные переменные

- `YANDEX_CLOUD_CHAPTERS_PREFIX` (default: `books`) - корневой префикс для объектов глав.
- `YANDEX_CLOUD_PATH_STYLE_ACCESS_ENABLED` (default: `true`) - path-style режим для S3-клиента.

## Связка с `application.properties`

В `src/main/resources/application.properties` эти env-переменные используются так:

- `yandex.cloud.storage.access-key=${YANDEX_CLOUD_ACCESS_KEY}`
- `yandex.cloud.storage.secret-key=${YANDEX_CLOUD_SECRET_KEY}`
- `yandex.cloud.storage.bucket=${YANDEX_CLOUD_BUCKET}`
- `yandex.cloud.storage.chapters-prefix=${YANDEX_CLOUD_CHAPTERS_PREFIX:books}`
- `yandex.cloud.storage.path-style-access-enabled=${YANDEX_CLOUD_PATH_STYLE_ACCESS_ENABLED:true}`

Дополнительно (обычно не меняются):

- `yandex.cloud.storage.endpoint=https://storage.yandexcloud.net`
- `yandex.cloud.storage.region=ru-central1`
- `yandex.cloud.storage.presigned-url-expiration-hours=1`

