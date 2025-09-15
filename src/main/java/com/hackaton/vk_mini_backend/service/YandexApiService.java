package com.hackaton.vk_mini_backend.service;

import com.hackaton.vk_mini_backend.config.YandexConfig;
import com.hackaton.vk_mini_backend.dto.ChatRequestDto;
import com.hackaton.vk_mini_backend.dto.CourseDto;
import com.hackaton.vk_mini_backend.dto.MessageDto;
import com.hackaton.vk_mini_backend.dto.UserProfileDto;
import com.hackaton.vk_mini_backend.dto.request.RecommendationRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class YandexApiService {
    private final YandexRequestService yandexRequestService;

    private final YandexConfig yandexConfig;

    public Map<String, Object> processChatRequest(ChatRequestDto request) {
        log.info("Получен запрос на чат");
        try {
            List<MessageDto> messages = request.getMessages();
            if (messages == null || messages.isEmpty()) {
                log.warn("Поле messages обязательно и должно быть массивом");
                return Map.of(
                        "error", "Bad Request",
                        "message", "Поле messages обязательно и должно быть массивом");
            }

            for (int i = 0; i < messages.size(); i++) {
                MessageDto msg = messages.get(i);
                if (msg.getRole() == null || msg.getText() == null) {
                    log.warn("Сообщение {} имеет неверный формат. Требуются поля role и text", i + 1);
                    return Map.of(
                            "error",
                            "Bad Request",
                            "message",
                            "Сообщение " + (i + 1) + " имеет неверный формат. Требуются поля role и text");
                }
            }

            String aiResponse = yandexRequestService.callYandexGpt(messages, 0.3, 1000);
            log.info("Получен ответ от YandexGPT для чата");

            return Map.of("success", true, "response", aiResponse);
        } catch (HttpClientErrorException e) {
            log.error("❌ Ошибка API YandexGPT: статус={}, ответ={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return Map.of("error", "YandexGPT API Error", "message", e.getResponseBodyAsString());
        } catch (Exception e) {
            if (e.getMessage().contains("connect")) {
                log.error("❌ Не удалось подключиться к API YandexGPT: {}", e.getMessage(), e);
                return Map.of(
                        "error", "Service Unavailable",
                        "message", "Не удалось подключиться к API YandexGPT");
            }
            if (e.getMessage().contains("YANDEX_API_KEY") || e.getMessage().contains("YANDEX_FOLDER_ID")) {
                log.error("❌ Проблема с конфигурацией YandexGPT: {}", e.getMessage(), e);
                return Map.of(
                        "error", "Configuration Error",
                        "message", e.getMessage(),
                        "details", "Установите переменные окружения YANDEX_API_KEY и YANDEX_FOLDER_ID");
            }
            log.error("❌ Ошибка при обращении к нейросети: {}", e.getMessage(), e);
            return Map.of(
                    "error", "Internal Server Error",
                    "message", "Ошибка при обращении к нейросети",
                    "details", e.getMessage());
        }
    }

    public Map<String, Object> processRecommendations(RecommendationRequestDto request) {
        log.info("Получен запрос на рекомендации курсов");
        log.info(
                "Данные запроса: hasUserProfile={}, coursesCount={}",
                request.getUserProfile() != null,
                request.getCourses() != null ? request.getCourses().size() : 0);

        try {
            UserProfileDto userProfile = request.getUserProfile();
            List<CourseDto> courses = request.getCourses();

            if (userProfile == null || courses == null || courses.isEmpty()) {
                if (courses == null || courses.isEmpty()) {
                    log.info("Нет доступных курсов для рекомендации");
                    return Map.of(
                            "success",
                            true,
                            "recommendations",
                            Collections.emptyList(),
                            "message",
                            "Нет доступных курсов для рекомендации");
                }
                log.warn("Поля userProfile и courses обязательны");
                return Map.of(
                        "error", "Bad Request",
                        "message", "Поля userProfile и courses обязательны");
            }

            StringBuilder userDescription = new StringBuilder("Пользователь");
            if (userProfile.getFirstName() != null || userProfile.getLastName() != null) {
                userDescription
                        .append(" ")
                        .append(Optional.ofNullable(userProfile.getFirstName()).orElse(""))
                        .append(" ")
                        .append(Optional.ofNullable(userProfile.getLastName()).orElse(""))
                        .append("".trim());
            }
            if (userProfile.getAge() != null) {
                userDescription
                        .append(", возраст ")
                        .append(userProfile.getAge())
                        .append(" лет");
                if (userProfile.getAge() >= 50) {
                    userDescription.append(" (интересуется здоровьем и активным долголетием)");
                } else if (userProfile.getAge() >= 30) {
                    userDescription.append(" (интересуется карьерным ростом и саморазвитием)");
                } else {
                    userDescription.append(" (интересуется новыми навыками и образованием)");
                }
            }
            if (userProfile.getSex() != null) {
                if (userProfile.getSex() == 1) {
                    userDescription.append(", женщина");
                } else if (userProfile.getSex() == 2) {
                    userDescription.append(", мужчина");
                }
            }
            if (userProfile.getCity() != null) {
                userDescription.append(", проживает в городе ").append(userProfile.getCity());
            }
            if (userProfile.getInterests() != null
                    && !userProfile.getInterests().isEmpty()) {
                userDescription
                        .append(". Предполагаемые интересы: ")
                        .append(String.join(", ", userProfile.getInterests()));
            }

            List<CourseDto> limitedCourses = courses.subList(0, Math.min(50, courses.size()));
            String coursesList = limitedCourses.stream()
                    .map(c -> (limitedCourses.indexOf(c) + 1) + ". ID: \"" + c.getId() + "\" | \"" + c.getTitle()
                            + "\" | Категория: " + (c.getCategory() != null ? c.getCategory() : "Не указана"))
                    .collect(Collectors.joining("\n"));

            String prompt =
                    "Ты эксперт по рекомендации образовательных курсов для пользователей университета третьего возраста.\n\n"
                            + "ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ:\n"
                            + userDescription + "\n\n" + "ДОСТУПНЫЕ КУРСЫ:\n"
                            + coursesList + "\n\n" + "ПРАВИЛА РЕКОМЕНДАЦИИ:\n"
                            + "- Учитывай возраст: людям 50+ больше подходят курсы по здоровью, компьютерной грамотности\n"
                            + "- Учитывай пол: женщинам чаще интересны психология, здоровье; мужчинам - техника, право\n"
                            + "- Учитывай город: в крупных городах больше интереса к IT и бизнесу\n"
                            + "- Выбери РОВНО 3 самых подходящих курса из списка выше\n"
                            + "- ВАЖНО: используй только ID курсов из предоставленного списка\n\n"
                            + "ОТВЕТ должен содержать ТОЛЬКО JSON-массив с ID курсов:\n"
                            + "[\"id1\", \"id2\", \"id3\"]";

            List<MessageDto> messages = new ArrayList<>();
            MessageDto systemMessage = MessageDto.builder()
                    .role("system")
                    .text(
                            "Ты эксперт по рекомендации образовательных курсов. Анализируешь профиль пользователя и рекомендуешь наиболее подходящие курсы. Всегда отвечаешь только JSON-массивом ID курсов без дополнительного текста.")
                    .build();
            messages.add(systemMessage);

            MessageDto userMessage =
                    MessageDto.builder().role("user").text(prompt).build();
            messages.add(userMessage);

            String aiResponse = yandexRequestService.callYandexGpt(messages, 0.2, 300);
            log.info("Получен ответ от ИИ: {}", aiResponse);

            List<String> recommendedIds = new ArrayList<>();
            try {
                String cleanResponse = aiResponse.trim();
                String jsonMatch = cleanResponse.replaceAll("^.*(\\[.*?\\]).*$", "$1");
                if (jsonMatch.startsWith("[")) {
                    List<?> parsed =
                            Arrays.asList(jsonMatch.replaceAll("[\\[\\]\"]", "").split(",\\s*"));
                    List<String> validIds =
                            courses.stream().map(CourseDto::getId).toList();
                    recommendedIds = parsed.stream()
                            .filter(id -> id instanceof String && validIds.contains(id))
                            .map(Object::toString)
                            .limit(3)
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("Ошибка парсинга ответа ИИ: {}", e.getMessage(), e);
                log.warn("Исходный ответ: {}", aiResponse);
            }

            if (recommendedIds.isEmpty()) {
                log.info("Используется резервный алгоритм");
                recommendedIds = courses.stream().limit(3).map(CourseDto::getId).collect(Collectors.toList());
            }

            log.info("Рекомендации готовы: {}", recommendedIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recommendations", recommendedIds);
            if (recommendedIds.isEmpty()) {
                response.put("fallback", true);
            }
            return response;
        } catch (Exception e) {
            log.error("Ошибка в рекомендациях: {}", e.getMessage(), e);

            List<String> fallbackIds =
                    Optional.ofNullable(request.getCourses()).orElse(Collections.emptyList()).stream()
                            .limit(3)
                            .map(CourseDto::getId)
                            .toList();

            return Map.of(
                    "success",
                    true,
                    "recommendations",
                    fallbackIds,
                    "fallback",
                    true,
                    "error",
                    "Использован резервный алгоритм из-за ошибки ИИ");
        }
    }
}
