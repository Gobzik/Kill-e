# MVVM + Clean Architecture - Kotlin Spring Boot

## Структура проекта

```
src/main/kotlin/com/yourapp/
├── application/              # Application Layer (Use Cases)
│   ├── usecase/             # Бизнес-логика приложения
│   │   └── UseCase.kt       # Базовый интерфейс для всех Use Cases
│   └── dto/                 # Data Transfer Objects
│       ├── request/
│       └── response/
│           └── ApiResponse.kt
│
├── domain/                  # Domain Layer (Чистый Kotlin, без зависимостей)
│   ├── model/
│   ├── repository/         # Интерфейсы репозиториев
│   └── exception/
│       └── DomainException.kt
│
├── infrastructure/          # Infrastructure Layer (Технические детали)
│   ├── persistence/
│   │   ├── entity/         # JPA Entity (представление в БД)
│   │   ├── repository/     # JPA Repository (Spring Data)
│   │   └── adapter/        # Адаптеры (реализация репозиториев)
│   ├── external/           # Внешние сервисы
│   │   └── ExternalService.kt
│   └── config/             # Конфигурация
│       └── DatabaseConfig.kt
│
├── presentation/            # Presentation Layer (UI/API)
│   ├── controller/         # REST Controllers
│   ├── viewmodel/          # ViewModels
│   └── mapper/             # Преобразователи между слоями
│
└── di/                      # Dependency Injection
    └── BeanConfiguration.kt
```

## Архитектурные слои

### 1. **Domain Layer** (Доменный слой)
- **Чистый Kotlin** без зависимостей от фреймворков
- Содержит бизнес-логику и правила
- Доменные модели (`User`, `Role`)
- Интерфейсы репозиториев
- Доменные исключения

### 2. **Application Layer** (Слой приложения)
- **Use Cases** - прикладная бизнес-логика
- Координирует выполнение операций
- DTO для передачи данных
- Использует domain-интерфейсы

### 3. **Infrastructure Layer** (Инфраструктурный слой)
- Реализация технических деталей
- JPA Entities для БД
- Адаптеры репозиториев
- Внешние сервисы
- Конфигурация

### 4. **Presentation Layer** (Слой представления)
- **Controllers** - REST API endpoints
- **ViewModels** - управление состоянием UI
- **Mappers** - преобразование данных

### 5. **DI Layer** (Внедрение зависимостей)
- Конфигурация бинов
- Связывание интерфейсов с реализациями

## Поток данных

```
Controller → ViewModel → UseCase → Repository Interface
                                          ↓
                                    Repository Adapter → JPA Repository → Database
```

## Принципы

1. **Dependency Rule**: Зависимости направлены внутрь (к domain)
2. **Single Responsibility**: Каждый класс имеет одну ответственность
3. **Interface Segregation**: Domain зависит только от интерфейсов
4. **Inversion of Control**: Через Spring DI

## Преимущества архитектуры

 **Тестируемость** - каждый слой можно тестировать независимо  
 **Переиспользование** - бизнес-логика не зависит от UI  
 **Масштабируемость** - легко добавлять новые фичи  
 **Гибкость** - можно менять технологии без изменения бизнес-логики  
 **Чистота кода** - четкое разделение ответственности
