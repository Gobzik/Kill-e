# Техническое задание: Android Application
# Интерактивное приложение для чтения и прослушивания книг

## 1. Назначение

Android приложение предоставляет пользователям:
-  Чтение книг с синхронизированным аудио
-  Прослушивание книг с подсветкой текста
-  Синхронизацию прогресса между устройствами
-  Отслеживание прогресса чтения
-  Офлайн режим (кеширование книг)

---

## 2. Архитектура

**Паттерн:** MVVM (Model-View-ViewModel)
**Язык:** Kotlin 100%
**Минимальная версия:** Android 8.0 (API 26)
**Целевая версия:** Android 14 (API 34)

### Технологический стек

#### Core
- **Kotlin** 1.9+
- **Kotlin Coroutines** для асинхронности
- **Kotlin Flow** для реактивных потоков

#### UI
- **Jetpack Compose** - современный декларативный UI
- **Material Design 3**
- **Navigation Compose**

#### Architecture Components
- **ViewModel** - управление UI состоянием
- **LiveData / StateFlow** - реактивные данные
- **Room** - локальная БД для офлайн режима
- **WorkManager** - фоновые задачи
- **DataStore** - хранение настроек

#### Networking
- **Retrofit** - HTTP клиент
- **OkHttp** - низкоуровневый HTTP
- **Moshi** - JSON парсинг
- **Stomp** - WebSocket для real-time синхронизации

#### Audio
- **ExoPlayer** - продвинутый аудиоплеер
- **MediaSession** - интеграция с системой
- **AudioFocus** - управление аудиофокусом

#### DI (Dependency Injection)
- **Hilt** (Dagger)

#### Testing
- **JUnit** - unit тесты
- **Mockk** - моки
- **Espresso** - UI тесты

---

## 3. Архитектурные слои

```
app/
├── data/              # Data Layer
│   ├── remote/        # API и WebSocket
│   ├── local/         # Room Database
│   ├── repository/    # Repository implementations
│   └── model/         # Data models (entities, DTOs)
│
├── domain/            # Domain Layer
│   ├── model/         # Domain models
│   ├── repository/    # Repository interfaces
│   └── usecase/       # Business logic (Use Cases)
│
├── presentation/      # Presentation Layer (MVVM)
│   ├── ui/
│   │   ├── screen/    # Compose screens
│   │   ├── component/ # Reusable components
│   │   └── theme/     # Material Theme
│   ├── viewmodel/     # ViewModels
│   └── navigation/    # Navigation logic
│
├── di/                # Dependency Injection
│   └── module/        # Hilt modules
│
└── util/              # Utilities
    ├── audio/         # Audio player helper
    ├── sync/          # Sync manager
    └── extension/     # Kotlin extensions
```

---

## 4. Основные экраны (Screens)

### 4.1 Splash Screen
- Загрузка приложения
- Проверка аутентификации
- Инициализация

### 4.2 Auth Screens
- **LoginScreen** - вход в систему
- **RegisterScreen** - регистрация
- **ForgotPasswordScreen** - восстановление пароля

### 4.3 Main Screens
- **LibraryScreen** - библиотека книг (список)
- **BookDetailScreen** - детали книги
- **ReaderScreen** - экран чтения с аудио
- **ProfileScreen** - профиль пользователя
- **SettingsScreen** - настройки приложения

---

## 5. Ключевые фичи

### 5.1 Синхронизированное чтение/прослушивание
**Основная фича проекта**

#### Режим "Чтение с аудио"
- Текст отображается на экране
- Аудио воспроизводится синхронно
- Текущее слово/предложение подсвечивается
- Пользователь может:
  - Коснуться слова → аудио перемотается к этому слову
  - Пауза/плей аудио
  - Изменить скорость воспроизведения (0.5x - 2.0x)

#### Режим "Только аудио"
- Минималистичный UI
- Большие кнопки управления
- Визуализация прогресса
- Блокировка экрана с продолжением воспроизведения

#### Режим "Только текст"
- Обычный текстовый ридер
- Настройка шрифта, размера, фона
- Скроллинг

### 5.2 Офлайн режим
- Скачивание книг для чтения офлайн
- Кеширование аудио
- Синхронизация при восстановлении соединения

### 5.3 Audio Player Features
- Play/Pause
- Rewind/Forward (10 сек)
- Speed control (0.5x, 0.75x, 1x, 1.25x, 1.5x, 2x)
- Sleep timer
- Уведомления с управлением воспроизведением

### 5.4 Кастомизация
- Тема (Светлая/Темная/Системная)
- Размер шрифта
- Тип шрифта
- Цвет подсветки
- Фон экрана чтения

---

## 6. Нефункциональные требования

### 6.1 Производительность
-  Плавная прокрутка (60 FPS)
-  Быстрое переключение глав (<500ms)
-  Синхронизация подсветки текста без задержек (<50ms)
-  Отзывчивый UI при работе с аудио

### 6.2 Надежность
-  Автоматическое переподключение WebSocket
-  Сохранение прогресса при крэше
-  Graceful degradation при отсутствии сети

### 6.3 Безопасность
-  Безопасное хранение токенов (EncryptedSharedPreferences)
-  HTTPS для всех запросов
-  Валидация данных от сервера

### 6.4 UX
-  Интуитивный интерфейс
-  Адаптивность под разные размеры экранов
-  Accessibility (TalkBack)

---

## 7. Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## 15. Roadmap

### MVP (Phase 1)
-  Аутентификация
-  Библиотека книг
-  Чтение с синхронизированным аудио
-  Прогресс синхронизация

### Phase 2
- Офлайн режим
- Закладки
- Заметки
- Статистика чтения

### Phase 3
- Социальные функции
- Рекомендации
- Достижения

---