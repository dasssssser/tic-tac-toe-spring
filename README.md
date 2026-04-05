# Крестики-нолики (Tic-Tac-Toe) на Spring Boot

REST API для игры в крестики-нолики с поддержкой многопользовательского режима (PVP) и игры с компьютером (PVE).  
Компьютер использует алгоритм **Минимакс** для выбора оптимального хода.

---

## 🚀 Технологии

- Java 21
- Spring Boot 3.1.5
- Spring Security (Basic Authentication)
- Spring Data JPA
- PostgreSQL
- Gradle
- JUnit 5 + Mockito

---

## 📁 Структура проекта
src/main/java/com/example/tictactoe/
├── datasource/ # Слой работы с данными
│ ├── entity/ # JPA сущности
│ │ ├── GameEntity.java # Сущность игры
│ │ ├── UserEntity.java # Сущность пользователя
│ │ └── StatusGame.java # Enum статусов игры
│ ├── mapper/ # Мапперы domain → entity
│ ├── repository/ # JPA репозитории (CrudRepository)
├── di/ # Конфигурация Spring Security
│ ├── AuthFilter.java # Фильтр Basic аутентификации
│ └── SecurityConfig.java # Конфигурация безопасности
├── domain/ # Бизнес-логика
│ ├── model/ # Модели домена
│ │ ├── GameField.java # Игровое поле 3x3
│ │ └── GameModel.java # Модель игры
│ └── service/ # Сервисы
│ ├── Service.java # Интерфейс игры
│ ├── ServiceImpl.java # Реализация с минимаксом
│ ├── UserService.java # Интерфейс пользователей
│ └── UserServiceImpl.java # Регистрация/авторизация
└── web/ # REST API
├── controller/ # Контроллеры
│ ├── AuthController.java # Аутентификация
│ └── GameController.java # Игровые эндпоинты
├── mapper/ # Мапперы domain → web
├── model/ # DTO
│ ├── GameWebDto.java # DTO игры
│ ├── MoveRequest.java # Запрос хода
│ ├── SignUpRequest.java # Запрос регистрации
│ └── ErrorResponse.java # Ответ с ошибкой

text

---

## 📊 Статусы игры

| Статус         | Описание                                      |
|----------------|-----------------------------------------------|
| `WAITING`      | Ожидание второго игрока                       |
| `PLAYER_TURN`  | Ход игрока (определяется по `currentTurnId`) |
| `WIN`          | Победа (победитель в `winnerId`)              |
| `DRAW`         | Ничья                                         |

---

## 🔐 Аутентификация

- Используется **Basic Authentication**
- Пароли хранятся в зашифрованном виде (BCrypt)
- Публичные эндпоинты: `/auth/register`, `/auth/login`

---

## 🎮 Эндпоинты API

### Аутентификация

| Метод | Эндпоинт           | Описание                    |
|-------|--------------------|-----------------------------|
| POST  | `/auth/register`   | Регистрация нового пользователя |
| POST  | `/auth/login`      | Авторизация (возвращает UUID) |

### Игра

| Метод   | Эндпоинт                    | Описание                          |
|---------|-----------------------------|-----------------------------------|
| POST    | `/game?mode=PVP/PVE`        | Создание новой игры               |
| GET     | `/game/available`           | Получение доступных игр (WAITING) |
| POST    | `/game/{gameId}/join`       | Присоединение к игре              |
| POST    | `/game/{gameId}`            | Сделать ход (с `MoveRequest`)     |
| GET     | `/game/{gameId}`            | Получение текущего состояния игры |
| GET     | `/user/{userId}`            | Получение информации о пользователе |

---

## 📦 Примеры запросов

### Регистрация
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"login":"alice","password":"alice123"}'
Авторизация
bash
curl -X POST http://localhost:8080/auth/login \
  -H "Authorization: Basic YWxpY2U6YWxpY2UxMjM="
Создание игры (PVP)
bash
curl -X POST "http://localhost:8080/game?mode=PVP" \
  -H "X-Player-Id: <UUID_ALICE>"
Присоединение к игре
bash
curl -X POST "http://localhost:8080/game/<GAME_ID>/join" \
  -H "X-Player-Id: <UUID_BOB>"
Сделать ход
bash
curl -X POST "http://localhost:8080/game/<GAME_ID>" \
  -H "X-Player-Id: <UUID_ALICE>" \
  -H "Content-Type: application/json" \
  -d '{"row":0,"col":0}'
Получить игру
bash
curl -X GET "http://localhost:8080/game/<GAME_ID>"
Получить пользователя
bash
curl -X GET "http://localhost:8080/user/<UUID_USER>"
🧠 Алгоритм Минимакс
Используется для режима PVE (игра против компьютера)

Компьютер играет ноликами (значок 2)

Игрок играет крестиками (значок 1)

Алгоритм рекурсивно перебирает возможные ходы и выбирает оптимальный

🗄️ База данных
Таблица users
Колонка	Тип	Описание
id	UUID (PK)	Идентификатор
login	VARCHAR	Логин (уникальный)
password	VARCHAR	Зашифрованный пароль
Таблица games
Колонка	Тип	Описание
id	UUID (PK)	Идентификатор игры
board	TEXT	JSON представление поля
status	VARCHAR	WAITING/PLAYER_TURN/WIN/DRAW
player1_id	UUID	Первый игрок (X)
player2_id	UUID	Второй игрок (O)
current_turn_id	UUID	Чей сейчас ход
game_mode	VARCHAR	PVP или PVE
winner_id	UUID	Победитель (если есть)
🧪 Запуск тестов
bash
./gradlew test
Всего тестов: 42

ServiceImplTest — тесты бизнес-логики

AuthControllerTest — тесты аутентификации

