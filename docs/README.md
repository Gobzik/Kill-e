#  Техническая документация проекта
# Интерактивное приложение для чтения и прослушивания книг

---

##  Содержание документации

### 1. Технические задания

#### 1.1 [Backend API](./TZ_BACKEND_API.md) 
Полное техническое задание для серверной части:
- REST API спецификация
- Схема базы данных PostgreSQL
- Интеграция с TTS/STT
- JWT аутентификация
- Примеры запросов и ответов

**Технологии:** Spring Boot 4, Kotlin, PostgreSQL

#### 1.2 [Android Application](./TZ_ANDROID_APP.md) 
Техническое задание для мобильного приложения:
- MVVM архитектура
- Jetpack Compose UI
- Синхронное чтение и прослушивание
- ExoPlayer для аудио
- Real-time синхронизация
- Офлайн режим

**Технологии:** Kotlin, Jetpack Compose, ExoPlayer

#### 1.3 [Admin Web Panel](./TZ_ADMIN_PANEL.md)
Техническое задание для веб-панели администратора:
- SPA на React/Angular
- Управление книгами и главами
- Rich text editor
- Audio waveform visualization
- Редактор тайм-маркеров
- Аналитика и статистика

**Технологии:** React/TypeScript

---

### 2. Требования к системе

#### 2.1 [Функциональные и нефункциональные требования](./REQUIREMENTS.md)
Полный список требований с приоритетами:
- **Функциональные требования (FR)**
  - 12 категорий (аутентификация, книги, главы, синхронизация и т.д.)
  - 100+ требований с приоритетами
- **Нефункциональные требования (NFR)**
  - Производительность
  - Масштабируемость
  - Надежность
  - Безопасность
  - Usability
- **Ограничения**
- **Приоритизация (MVP → Phase 2 → Phase 3)**

---

### 3. Архитектура

#### 3.1 [Архитектурное описание](./ARCHITECTURE.md)
MVVM + Clean Architecture:
- Структура проекта
- Слои архитектуры (Domain, Application, Infrastructure, Presentation)
- Принципы проектирования

Визуальные схемы:
- C4 диаграммы
- Sequence диаграммы
- Поток данных
- Взаимодействие компонентов

---

##  Ключевые особенности проекта

### Основная фича: Синхронное чтение и прослушивание

Проект демонстрирует уникальную функцию:
-  Текст отображается на экране
-  Аудио воспроизводится синхронно
-  Текущее слово/предложение подсвечивается в реальном времени
-  Клик по тексту → аудио перематывается к этому слову

### Технологические демонстрации

1. **Работа с аудио в Android**
   - ExoPlayer integration
   - MediaSession API
   - Background playback
   - Notifications

2. **Clean Architecture**
   - Domain layer (чистый Kotlin)
   - Use Cases для бизнес-логики
   - Repository Pattern
   - Dependency Inversion

3. **Современный Android UI**
   - Jetpack Compose
   - Material Design 3
   - MVVM с StateFlow

4. **Производительность**
   - Оптимизация подсветки текста (<50ms latency)
   - Плавный UI (60 FPS)
   - Эффективная работа с памятью

---


---

##  Workflow: Создание синхронизированного контента

### Шаг 1: Создание книги (Admin Panel)
1. Админ заходит в панель
2. Создает новую книгу (название, автор, описание)
3. Загружает обложку

### Шаг 2: Добавление глав (Admin Panel)
1. Добавляет главу
2. Вводит/вставляет текст в Rich Text Editor
3. Загружает аудиофайл

### Шаг 3: Публикация
1. Активирует книгу
2. Книга становится доступна пользователям

### Шаг 4: Чтение (Android App)
1. Пользователь выбирает книгу
2. Открывает главу
3. Нажимает Play
4. Аудио играет, текст подсвечивается
5. Прогресс автоматически сохраняется
6. При открытии на другом устройстве → позиция синхронизирована

---

##  Метрики качества

### Производительность
-  API latency: ≤ 300 мс
-  Подсветка текста: ≤ 50 мс
-  UI фреймрейт: 60 FPS

### Надежность
-  Uptime: 99.5%
-  Автоматическое переподключение
-  Optimistic locking
-  Transaction management

### Безопасность
-  HTTPS only
-  JWT с коротким TTL
-  Rate limiting
-  Input validation

### Тестирование
-  Unit тесты: ≥70% coverage
-  Integration тесты
-  E2E тесты

---

## Технологии

### Backend
- **Spring Boot** 4.0.2
- **Kotlin** 2.2.21
- **PostgreSQL** 15+
- **JWT** (jjwt)
- **WebSocket** (STOMP)
- **Swagger/OpenAPI** 3.0

### Android
- **Kotlin** 1.9+
- **Jetpack Compose**
- **Material Design 3**
- **ExoPlayer** (Media3)
- **Room** (SQLite)
- **Hilt** (DI)
- **Retrofit** (HTTP)
- **Coroutines + Flow**

### Admin Panel
- **React** 18+
- **JavaScript** 5+
- **Material-UI** / **Ant Design**
- **React Query**
- **Wavesurfer.js**

---

##  Структура репозитория

```
project-root/
├── backend/               # Spring Boot Backend
│   ├── src/
│   │   ├── main/kotlin/com/yourapp/
│   │   │   ├── application/    # Use Cases, DTOs
│   │   │   ├── domain/         # Domain Models
│   │   │   ├── infrastructure/ # JPA, Storage
│   │   │   ├── presentation/   # Controllers, ViewModels
│   │   │   └── di/             # DI Configuration
│   │   └── resources/
│   └── build.gradle.kts
│
├── android/               # Android Application
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/kotlin/com/yourapp/
│   │   │   │   ├── data/         # Repositories, API
│   │   │   │   ├── domain/       # Domain Models, Use Cases
│   │   │   │   ├── presentation/ # Compose UI, ViewModels
│   │   │   │   └── di/           # Hilt Modules
│   │   │   └── res/
│   │   └── build.gradle.kts
│   └── gradle/
│
├── admin-panel/           # React Admin Panel
│   ├── src/
│   │   ├── components/    # UI Components
│   │   ├── pages/         # Pages
│   │   ├── features/      # Feature modules
│   │   ├── services/      # API services
│   │   ├── store/         # State management
│   │   └── types/         # TypeScript types
│   ├── public/
│   └── package.json
│
└── docs/                  # Документация
    ├── TZ_BACKEND_API.md
    ├── TZ_ANDROID_APP.md
    ├── TZ_ADMIN_PANEL.md
    ├── REQUIREMENTS.md
    ├── ARCHITECTURE.md
    └── PROJECT_INDEX.md (этот файл)
```

---

##  Ссылки на документацию

| Документ           | Описание                 | Ссылка                                                                 |
|--------------------|--------------------------|------------------------------------------------------------------------|
| **Backend API ТЗ** | Полная спецификация API  | [TZ_BACKEND_API.md](./TZ_BACKEND_API.md)                               |
| **Android ТЗ**     | Спецификация Android app | [TZ_ANDROID_APP.md](./TZ_ANDROID_APP.md)                               |
| **Admin Panel ТЗ** | Спецификация веб-панели  | [TZ_ADMIN_PANEL.md](./TZ_ADMIN_PANEL.md)                               |
| **Требования**     | FR + NFR                 | [REQUIREMENTS.md](./REQUIREMENTS.md)                                   |
| **Архитектура**    | Clean Architecture       | [ARCHITECTURE.md](./ARCHITECTURE.md)                                   |
| **Swagger**        | API документация         | http://localhost:8080/swagger-ui.html                                  |
| **C4**             | Архитектурная диаграмма  | [C4](https://miro.com/app/board/uXjVGMpY2_o=/?share_link_id=140586318) |
| **Sequence диаграмма** | Диаграмма процесса чтения | [Sequence](./Sequence.jpg)                                             |

---

