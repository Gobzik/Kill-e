# Как запустить проект и протестировать API

## 🚀 Запуск проекта

### Предварительные требования
- JDK 17 или выше
- Gradle (встроен в проект через Gradle Wrapper)

### Способ 1: Через Gradle (рекомендуется)

```powershell
# Из корневой папки проекта
.\gradlew.bat bootRun
```

### Способ 2: Через IDE (IntelliJ IDEA)

1. Откройте проект в IntelliJ IDEA
2. Найдите файл `KillEApplication.kt`
3. Нажмите на зелёную стрелку рядом с `main` функцией
4. Выберите "Run 'KillEApplication'"

### Проверка запуска

После успешного запуска вы увидите в консоли:
```
Started KillEApplicationKt in X.XXX seconds
```

По умолчанию приложение запускается на порту **8080**.

---

## 📚 Swagger UI - Интерактивная документация API

### Как открыть Swagger

После запуска приложения откройте браузер и перейдите по адресу:

```
http://localhost:8080/swagger-ui.html
```

Или альтернативный URL:

```
http://localhost:8080/swagger-ui/index.html
```

### Что вы увидите

Swagger предоставляет:
- 📋 Список всех доступных API эндпоинтов
- 📝 Описание каждого эндпоинта
- 🔧 Интерактивное тестирование (можно отправлять запросы прямо из браузера)
- 📊 Схемы данных (Request/Response)

---

## 🧪 Тестирование API через Swagger

### Пример: Создание ExampleEntity

1. **Откройте Swagger UI** по адресу `http://localhost:8080/swagger-ui.html`

2. **Найдите секцию "Examples"** - это контроллер для демонстрационной сущности

3. **Раскройте POST `/api/v1/examples`** - эндпоинт для создания новой задачи

4. **Нажмите "Try it out"**

5. **Введите JSON в поле Request Body**:
   ```json
   {
     "title": "Моя первая задача",
     "description": "Это демонстрационная задача для проверки DDD архитектуры",
     "priority": "HIGH",
     "estimatedEffort": 5
   }
   ```

6. **Нажмите "Execute"**

7. **Проверьте ответ** - вы должны увидеть:
   - Status: `201 Created`
   - Response Body с данными созданной задачи

### Пример: Получение всех ExampleEntity

1. **Раскройте GET `/api/v1/examples`**

2. **Нажмите "Try it out"**

3. **Нажмите "Execute"**

4. **Проверьте ответ** - вы увидите список всех созданных задач

---

## 📡 Тестирование через cURL (командная строка)

### Создание новой задачи

```powershell
curl -X POST "http://localhost:8080/api/v1/examples" `
  -H "Content-Type: application/json" `
  -d '{
    "title": "Тестовая задача",
    "description": "Описание задачи",
    "priority": "MEDIUM",
    "estimatedEffort": 3
  }'
```

### Получение всех задач

```powershell
curl -X GET "http://localhost:8080/api/v1/examples"
```

---

## 🗄️ База данных

Проект использует **H2 in-memory database** (встроенная БД в памяти).

### H2 Console

Для просмотра данных в БД:

1. Откройте в браузере:
   ```
   http://localhost:8080/h2-console
   ```

2. Введите настройки подключения:
   - **JDBC URL**: `jdbc:h2:mem:testdb`
   - **User Name**: `sa`
   - **Password**: _(оставьте пустым)_

3. Нажмите "Connect"

4. Выполните SQL запрос:
   ```sql
   SELECT * FROM EXAMPLE_ENTITIES;
   ```

---

## 📖 Структура API

### Базовый URL
```
http://localhost:8080/api/v1
```

### Доступные эндпоинты

#### Examples (демонстрационная сущность)

| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/v1/examples` | Получить все задачи |
| POST | `/api/v1/examples` | Создать новую задачу |

### Формат ответа

Все ответы обёрнуты в `ApiResponse`:

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "timestamp": 1706630400000
}
```

---

## 🔧 Решение проблем

### Порт 8080 уже занят

Если порт занят другим приложением, измените порт в `application.properties`:

```properties
server.port=8081
```

Затем перезапустите приложение и используйте новый порт:
```
http://localhost:8081/swagger-ui.html
```

### Приложение не запускается

1. Проверьте версию Java:
   ```powershell
   java -version
   ```
   Должна быть 17 или выше.

2. Очистите кэш Gradle:
   ```powershell
   .\gradlew.bat clean build
   ```

3. Проверьте логи на наличие ошибок

---

## 📚 Архитектура проекта

Проект демонстрирует **MVVM + Clean Architecture + DDD**:

```
├── domain/              # Бизнес-логика (богатые сущности)
│   ├── model/          # ExampleEntity с бизнес-правилами
│   └── repository/     # Интерфейсы репозиториев
├── application/        # Use Cases (сценарии использования)
│   ├── usecase/       # CreateExampleUseCase, GetAllExamplesUseCase
│   └── dto/           # Request/Response DTO
├── infrastructure/    # Технические детали (БД, внешние сервисы)
│   ├── persistence/   # JPA Entity, Repository, Adapter
│   └── config/        # Конфигурации (Swagger, БД)
└── presentation/      # REST контроллеры
    ├── controller/    # ExampleController
    └── mapper/        # Domain <-> DTO маппер
```

### Ключевые принципы

✅ **Dependency Inversion** - Domain не зависит от Infrastructure  
✅ **Rich Domain Model** - Бизнес-логика в доменных сущностях  
✅ **Use Cases** - Каждая операция = отдельный Use Case  
✅ **Separation of Concerns** - Чёткое разделение слоёв  
✅ **Immutability** - Доменные модели неизменяемы (copy на изменения)  

---

## 🎯 Что дальше?

1. **Изучите код** `ExampleEntity.kt` - это богатая доменная модель с бизнес-логикой
2. **Посмотрите Use Cases** - как координируется бизнес-логика
3. **Изучите Adapter** - как Domain отделён от Infrastructure
4. **Экспериментируйте** - создавайте задачи через Swagger и смотрите результаты

Удачи! 🚀
