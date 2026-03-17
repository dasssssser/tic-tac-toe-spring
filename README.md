# Крестики-нолики (Tic-Tac-Toe) на Spring Boot

REST API для игры в крестики-нолики с компьютером.  
Компьютер использует алгоритм **Минимакс** для выбора оптимального хода.

---

## 🚀 Технологии

- Java 21
- Spring Boot 3.1.5
- Gradle (Kotlin DSL)
- MapStruct
- ConcurrentHashMap (хранение игр в памяти)

---

## 📁 Структура проекта
src/main/java/com/example/tictactoe/
├── datasource/ # Слой работы с данными
│ ├── mapper/ # Мапперы для преобразования domain → datasource
│ ├── model/ # Модели для хранения (Storage, StatusGame)
│ └── repository/ # Репозиторий с ConcurrentHashMap
├── di/ # Конфигурация Spring (AppConfig)
├── domain/ # Бизнес-логика
│ ├── model/ # Модели (GameField, GameModel)
│ └── service/ # Сервис с минимаксом и валидацией
└── web/ # REST API
├── controller/ # Контроллер
├── mapper/ # Мапперы domain → web
└── model/ # DTO (GameWebDto, ErrorResponse)

---

