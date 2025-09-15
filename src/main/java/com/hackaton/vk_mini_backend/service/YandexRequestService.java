package com.hackaton.vk_mini_backend.service;

import com.hackaton.vk_mini_backend.dto.MessageDto;
import com.hackaton.vk_mini_backend.dto.request.YandexRequest;
import com.hackaton.vk_mini_backend.dto.response.YandexResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class YandexRequestService {
    @Value("${yandex.api-key}")
    private String apiKey;

    @Value("${yandex.folder-id}")
    private String folderId;

    @Value("${yandex.mock-mode:false}")
    private boolean mockMode;

    private final RestTemplate restTemplate = new RestTemplate();

    public String callYandexGpt(List<MessageDto> messages, double temperature, int maxTokens) throws Exception {
        log.info("🔧 Проверка конфигурации YandexGPT:");
        log.info(
                "🔑 API Key: {}",
                apiKey != null
                        ? "установлен (" + apiKey.substring(0, Math.min(8, apiKey.length())) + "...)"
                        : "НЕ УСТАНОВЛЕН");
        log.info("📁 Folder ID: {}", folderId != null ? folderId : "НЕ УСТАНОВЛЕН");
        log.info("🎭 Mock Mode: {}", mockMode);

        // Проверяем, включен ли мок-режим
        if (mockMode) {
            log.info("🎭 Используется мок-режим для YandexGPT");
            return generateMockResponse(messages);
        }

        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("test-api-key")) {
            throw new Exception(
                    "Не указан YANDEX_API_KEY. Установите переменную окружения YANDEX_API_KEY или включите мок-режим (YANDEX_MOCK_MODE=true)");
        }

        if (folderId == null || folderId.trim().isEmpty() || folderId.contains("test-folder-id")) {
            throw new Exception(
                    "Не указан YANDEX_FOLDER_ID. Установите переменную окружения YANDEX_FOLDER_ID или включите мок-режим (YANDEX_MOCK_MODE=true)");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Api-Key " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> completionOptions = new HashMap<>();
        completionOptions.put("stream", false);
        completionOptions.put("temperature", temperature);
        completionOptions.put("maxTokens", maxTokens);

        String modelUri = "gpt://" + folderId + "/yandexgpt/latest";
        log.info("🤖 Model URI: {}", modelUri);

        YandexRequest requestBody = YandexRequest.builder()
                .messages(messages)
                .completionOptions(completionOptions)
                .modelUri(modelUri)
                .build();

        log.info("🔄 Отправка запроса в YandexGPT");
        log.info("📝 Число сообщений: {}", messages.size());

        HttpEntity<YandexRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<YandexResponse> response = restTemplate.exchange(
                    "https://llm.api.cloud.yandex.net/foundationModels/v1/completion",
                    HttpMethod.POST,
                    entity,
                    YandexResponse.class);

            String responseText = Optional.ofNullable(response.getBody())
                    .map(YandexResponse::getResult)
                    .map(YandexResponse.Result::getAlternatives)
                    .filter(alts -> !alts.isEmpty())
                    .map(List::getFirst)
                    .map(YandexResponse.Alternative::getMessage)
                    .map(MessageDto::getText)
                    .orElse("");

            if (responseText.isEmpty()) {
                log.error("❌ Пустой ответ от YandexGPT: {}", response.getBody());
                throw new Exception("Пустой ответ от YandexGPT");
            }

            log.info("✅ Получен ответ от YandexGPT, длина: {}", responseText.length());
            return responseText;

        } catch (HttpClientErrorException e) {
            log.error("❌ YandexGPT API ошибка: статус={}, ответ={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (RestClientException e) {
            log.error("❌ Ошибка при запросе в YandexGPT: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Генерирует мок-ответ для разработки без реального API
     */
    private String generateMockResponse(List<MessageDto> messages) {
        // Определяем тип запроса по содержимому сообщений
        String lastMessage =
                messages.isEmpty() ? "" : messages.get(messages.size() - 1).getText();

        if (lastMessage.contains("рекомендации") || lastMessage.contains("курс")) {
            // Мок-ответ для рекомендаций курсов
            log.info("🎭 Генерация мок-ответа для рекомендаций курсов");
            return "[\"course1\", \"course2\", \"course3\"]";
        } else {
            // Мок-ответ для чата
            log.info("🎭 Генерация мок-ответа для чата");
            return "Это мок-ответ от YandexGPT. Для использования реального API установите переменные окружения YANDEX_API_KEY и YANDEX_FOLDER_ID.";
        }
    }
}
