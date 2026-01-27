#  Техническое задание: Backend API - Интерактивное приложение для чтения и прослушивания книг

## 1. Назначение

Backend отвечает за:
-  Хранение книг, глав, текста и аудио-метаданных
-  Управление пользователями и аутентификацией
-  Хранение и синхронизацию прогресса чтения/прослушивания
-  Синхронизацию позиции между устройствами в реальном времени
- ️ Интеграцию с TTS (Text-to-Speech) / STT (Speech-to-Text) сервисами
-  Управление тайм-маркерами для синхронизации аудио и текста

---

## 2. Архитектура

**Тип:** Монолит на Spring Boot
**Язык:** Kotlin
**Паттерн:** MVVM + Clean Architecture

### Технологический стек
- **Framework:** Spring Boot 4.0.2
- **База данных:** PostgreSQL 15+
- **Аутентификация:** JWT (Access + Refresh tokens) + Spring Security
- **Формат данных:** JSON
- **Хранение аудио:** External Storage (AWS S3 / MinIO)
- **Документация API:** Swagger/OpenAPI 3.0

### Архитектурные принципы
-  RESTful API
-  Stateless
-  Clean Architecture (Domain, Application, Infrastructure, Presentation)
-  Repository Pattern
-  Use Cases для бизнес-логики

---

## 3. Основные сущности

### 3.1 User (Пользователь)

### 3.2 Book (Книга)

### 3.3 Chapter (Глава)

### 3.4 TimeMarker (Тайм-маркер)

### 3.5 ReadingProgress (Прогресс чтения)

---

## 4. API Endpoints

### 4.1 Аутентификация

| Метод | URL | Описание | Auth |
|-------|-----|----------|------|
| POST | `/api/auth/register` | Регистрация пользователя | - |
| POST | `/api/auth/login` | Вход (получение токенов) | - |
| POST | `/api/auth/refresh` | Обновление access token | Refresh Token |
| POST | `/api/auth/logout` | Выход из системы | JWT |

### 4.2 Книги

| Метод | URL | Описание | Auth |
|-------|-----|----------|------|
| GET | `/api/books` | Список всех книг | JWT |
| GET | `/api/books/{id}` | Детали книги | JWT |
| POST | `/api/books` | Создать книгу (admin) | JWT (ADMIN) |
| PUT | `/api/books/{id}` | Обновить книгу (admin) | JWT (ADMIN) |
| DELETE | `/api/books/{id}` | Удалить книгу (admin) | JWT (ADMIN) |

### 4.3 Главы

| Метод | URL | Описание | Auth |
|-------|-----|----------|------|
| GET | `/api/books/{bookId}/chapters` | Список глав книги | JWT |
| GET | `/api/chapters/{id}` | Детали главы (текст + аудио) | JWT |
| POST | `/api/chapters` | Создать главу (admin) | JWT (ADMIN) |

### 4.4 Прогресс чтения

| Метод | URL | Описание | Auth |
|-------|-----|----------|------|
| GET | `/api/progress/{bookId}` | Прогресс по книге | JWT |
| POST | `/api/progress` | Обновить прогресс | JWT |
| DELETE | `/api/progress/{bookId}` | Сбросить прогресс | JWT |

---

## 5. Нефункциональные требования

### 5.1 Производительность
-  **API Latency:** ≤ 300 мс
-  **Concurrent Users:** до 10,000

### 5.2 Безопасность
-  JWT с коротким TTL (15 мин access, 7 дней refresh)
-  HTTPS only
-  Rate limiting: 100 req/min
-  Optimistic locking для прогресса

### 5.3 Надежность
-  Transaction Management (ACID)
-  Retry Policy для внешних сервисов
-  Circuit Breaker для TTS/STT

---

## 6. База данных (PostgreSQL)

### Основные таблицы
- `users` - пользователи
- `books` - книги
- `chapters` - главы
- `time_markers` - тайм-маркеры
- `reading_progress` - прогресс чтения

**Полная схема см. в файле `DATABASE_SCHEMA.md`**

---

## 8. Документация

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Схема БД:** `docs/DATABASE_SCHEMA.md`

---
