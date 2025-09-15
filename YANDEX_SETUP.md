# Настройка YandexGPT API

## Проблема
При запросе к нейросети возникает ошибка:
```json
{
    "error": "YandexGPT API Error",
    "message": "{\"error\":{\"grpcCode\":3,\"httpCode\":400,\"message\":\"invalid model_uri\",\"httpStatus\":\"Bad Request\",\"details\":[]}}"
}
```

## Решение

### 1. Получите доступ к YandexGPT API

1. Перейдите в [консоль Yandex Cloud](https://console.cloud.yandex.ru/)
2. Войдите в свой аккаунт или создайте новый
3. Создайте новый проект или выберите существующий

### 2. Настройте сервисный аккаунт

1. Перейдите в раздел "Сервисные аккаунты"
2. Создайте новый сервисный аккаунт или используйте существующий
3. Назначьте роль `ai.languageModels.user` для работы с YandexGPT
4. Создайте API ключ для сервисного аккаунта

### 3. Получите необходимые данные

- **YANDEX_API_KEY**: API ключ сервисного аккаунта
- **YANDEX_FOLDER_ID**: ID папки (можно найти в URL или в интерфейсе)

### 4. Установите переменные окружения

#### Windows (PowerShell):
```powershell
$env:YANDEX_API_KEY="your-api-key-here"
$env:YANDEX_FOLDER_ID="your-folder-id-here"
```

#### Windows (Command Prompt):
```cmd
set YANDEX_API_KEY=your-api-key-here
set YANDEX_FOLDER_ID=your-folder-id-here
```

#### Linux/macOS:
```bash
export YANDEX_API_KEY="your-api-key-here"
export YANDEX_FOLDER_ID="your-folder-id-here"
```

### 5. Перезапустите приложение

После установки переменных окружения перезапустите Spring Boot приложение:

```bash
cd vk_mini_backend
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

### 6. Проверка

Теперь при запросе к нейросети в логах вы увидите:
- ✅ API Key: установлен (a1b2c3d4...)
- ✅ Folder ID: b1g1234567890abcdef
- ✅ Model URI: gpt://b1g1234567890abcdef/yandexgpt/latest

## Альтернативный способ (для разработки)

Если у вас нет доступа к YandexGPT API, можно временно использовать мок-ответы, изменив код в `YandexRequestService`.
