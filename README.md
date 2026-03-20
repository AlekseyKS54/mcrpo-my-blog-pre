# My Blog - Backend Application

Backend-приложение для блога на Spring Boot.

## Структура проекта

```
mcrpo-my-blog-pre/
├── src/main/java/com/myblog/
│   ├── controller/          # REST-контроллеры
│   ├── service/             # Бизнес-логика
│   ├── repository/          # Репозитории для работы с БД
│   ├── model/               # Модели данных
│   ├── dto/                 # Data Transfer Objects
│   └── exception/           # Обработка исключений
├── src/main/resources/      # Конфигурация и схема БД
├── src/test/                # Тесты
├── frontend/                # Frontend на React + Vite
└── pom.xml                  # Maven конфигурация
```

## Технологии

- **Java 17**
- **Spring Boot 4.0.3**
  - Spring Web
  - Spring Data JDBC
- **H2 Database** (in-memory)
- **Maven**
- **JUnit 5 + Mockito** (тестирование)

## Сборка проекта

### Требования

- JDK 17+
- Maven 3.6+

### Команды сборки

```bash
# Очистка и сборка проекта
mvn clean package

# Сборка без запуска тестов
mvn clean package -DskipTests

# Установка в локальный репозиторий Maven
mvn clean install
```

## Запуск тестов

```bash
# Запуск всех тестов
mvn test

# Запуск конкретного тестового класса
mvn test -Dtest=PostServiceTest

# Запуск с выводом в консоль
mvn test -Dtest=PostServiceTest -DfailIfNoTests=false
```

### Структура тестов

- **Unit-тесты** (`src/test/java/com/myblog/service/`) — тесты сервисов с использованием Mockito
- **Интеграционные тесты** (`src/test/java/com/myblog/controller/`) — тесты REST API через MockMvc
- **Репозиторные тесты** (`src/test/java/com/myblog/repository/`) — тесты Spring Data JDBC репозиториев

## Запуск приложения

### Способ 1: Через Maven (рекомендуется для разработки)

```bash
mvn spring-boot:run
```

Приложение будет доступно по адресу: `http://localhost:8080/api`

### Способ 2: Через JAR-файл

```bash
# Сначала соберите проект
mvn clean package

# Запустите JAR
java -jar target/my-blog-back-app-1.0.0.jar
```

### Способ 3: Из IDE

Запустите класс `com.myblog.MyBlogBackAppApplication` напрямую из IDE.

## Конфигурация

### База данных

Приложение использует H2 in-memory базу данных. Конфигурация находится в `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:blogdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
```

### H2 Console

Для отладки доступна H2 Console: `http://localhost:8080/api/h2-console`

- JDBC URL: `jdbc:h2:mem:blogdb`
- User: `sa`
- Password: (пусто)

## API Endpoints

### Посты

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/posts` | Получить список всех постов |
| GET | `/posts/{id}` | Получить пост по ID |
| POST | `/posts` | Создать новый пост |
| PUT | `/posts/{id}` | Обновить пост |
| DELETE | `/posts/{id}` | Удалить пост |
| POST | `/posts/{id}/likes` | Поставить лайк посту |
| DELETE | `/posts/{id}/likes` | Убрать лайк с поста |

### Комментарии

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/posts/{postId}/comments` | Получить комментарии к посту |
| POST | `/posts/{postId}/comments` | Добавить комментарий |
| PUT | `/posts/{postId}/comments/{commentId}` | Обновить комментарий |
| DELETE | `/posts/{postId}/comments/{commentId}` | Удалить комментарий |

## Frontend

Frontend находится в директории `frontend/`. Для запуска:

```bash
cd frontend
npm install
npm run dev
```

Frontend запускается на `http://localhost:3000`

## Проверка работы

```bash
# Получить список постов
curl http://localhost:8080/api/posts

# Создать пост
curl -X POST http://localhost:8080/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","text":"Content","tags":["tag1"]}'

# Поставить лайк
curl -X POST http://localhost:8080/api/posts/1/likes
```
