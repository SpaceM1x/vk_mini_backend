package com.hackaton.vk_mini_backend.service;

import com.hackaton.vk_mini_backend.config.security.jwt.JwtUtils;
import com.hackaton.vk_mini_backend.dto.ClsUserDto;
import com.hackaton.vk_mini_backend.dto.response.JwtResponse;
import com.hackaton.vk_mini_backend.exception.TokenRefreshException;
import com.hackaton.vk_mini_backend.model.ClsUser;
import com.hackaton.vk_mini_backend.model.RegUserToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для авторизации пользователей через VK API.
 * Обеспечивает взаимодействие с VK API, обработку ответов и генерацию JWT токенов.
 */
@Service
@Slf4j
public class VkAuthService {

    private static final String ERROR_KEY = "error";
    private static final String ERROR_MSG_KEY = "error_msg";
    private static final String RESPONSE_KEY = "response";
    private static final String ITEMS_KEY = "items";

    private static final String METHOD_PATH = "method";
    private static final String USERS_GET = "users.get";
    private static final String USER_IDS = "user_ids";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String VERSION = "v";
    private static final String VK_USER_PREFIX = "vk";

    private final RestTemplate restTemplate;
    private final ClsUserService userService;
    private final JwtUtils jwtUtils;
    private final RegUserTokenService refreshTokenService;

    /**
     * Версия VK API, используемая для запросов.
     * Значение загружается из конфигурации приложения с дефолтным значением 5.199.
     */
    @Value("${vk.api.version:5.199}")
    private String vkApiVersion;

    /**
     * URL для запросов к VK API.
     * Параметры запроса:
     *   Этот параметр обязателен для корректной работы метода users.get и успешной авторизации
     * - access_token=%s - токен доступа для авторизации запроса.
     *   При использовании сервисного ключа не привязан к конкретному пользователю
     * - v=%s - версия API ВКонтакте, определяет формат ответа и доступные поля
     */
    @Value("${vk.api.url}")
    private String vkApiUrl;

    /**
     * Сервисный ключ VK
     */
    @Value("${vk.app.service-key}")
    private String vkServiceKey;

    /**
     * Конструктор сервиса авторизации VK.
     *
     * @param restTemplate Клиент для HTTP-запросов к VK API
     * @param userService Сервис для работы с пользователями
     * @param jwtUtils Утилиты для работы с JWT токенами
     * @param refreshTokenService Сервис для работы с refresh токенами
     */
    public VkAuthService(
            final RestTemplate restTemplate,
            final ClsUserService userService,
            final JwtUtils jwtUtils,
            final RegUserTokenService refreshTokenService) {

        this.restTemplate = restTemplate;
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Анализирует и обрабатывает ответ VK API.
     * Метод учитывает различные форматы ответа (список или map).
     *
     * @param vkResp Ответ от VK API
     * @return Map с данными пользователя
     * @throws TokenRefreshException при отсутствии или неверном формате данных
     */
    private Map<?, ?> parseVkResponse(final Map<?, ?> vkResp) {
        Map<?, ?> result = null;

        Object response = vkResp.get(RESPONSE_KEY);
        if (response == null) {
            throw new TokenRefreshException("Ошибка VK-токена: поле 'response' отсутствует");
        } else {
            if (response instanceof List) {
                result = handleResponseAsList((List<?>) response);
            } else if (response instanceof Map) {
                result = handleResponseAsMap((Map<?, ?>) response);
            } else {
                throw new TokenRefreshException("Неизвестный формат ответа от VK API");
            }
        }

        return result;
    }

    /**
     * Обрабатывает ответ VK API в формате списка.
     *
     * @param responseList Список с данными от VK API
     * @return Map с данными пользователя (первый элемент списка)
     * @throws TokenRefreshException при пустом списке
     */
    private Map<?, ?> handleResponseAsList(final List<?> responseList) {
        if (responseList.isEmpty()) {
            throw new TokenRefreshException("Ошибка VK-токена: данные пользователя отсутствуют");
        }
        return (Map<?, ?>) responseList.get(0);
    }

    /**
     * Обрабатывает ответ VK API в формате карты с элементом 'items'.
     *
     * @param responseMap Карта с данными от VK API
     * @return Map с данными пользователя (первый элемент списка 'items')
     * @throws TokenRefreshException при отсутствии элемента 'items' или пустом списке
     */
    private Map<?, ?> handleResponseAsMap(final Map<?, ?> responseMap) {
        Object items = responseMap.get(ITEMS_KEY);
        if (items instanceof List) {
            List<?> itemsList = (List<?>) items;
            if (itemsList.isEmpty()) {
                throw new TokenRefreshException("Ошибка VK-токена: данные пользователя отсутствуют");
            }
            return (Map<?, ?>) itemsList.get(0);
        }
        throw new TokenRefreshException("Ошибка VK-токена: неверный формат данных пользователя");
    }

    /**
     * Извлекает ID пользователя VK из данных пользователя.
     *
     * @param vkUser Map с данными пользователя
     * @return ID пользователя VK в виде строки
     * @throws TokenRefreshException при отсутствии ID в данных
     */
    private String extractVkUserId(final Map<?, ?> vkUser) {
        Object id = vkUser.get("id");
        if (id == null) {
            throw new TokenRefreshException("Ошибка VK-токена: ID пользователя отсутствует");
        }
        return String.valueOf(id);
    }

    /**
     * Находит существующего пользователя в системе или создает нового на основе данных VK.
     *
     * @param login Логин пользователя
     * @param vkUser Map с данными пользователя VK
     * @return Объект пользователя системы
     */
    private ClsUser findOrCreateUser(final String login, final Map<?, ?> vkUser) {
        return userService.findByLogin(login).orElseGet(() -> createNewUser(login, vkUser));
    }

    /**
     * Создает нового пользователя в системе на основе данных VK.
     *
     * @param login Логин пользователя (формируется как "vk" + id пользователя VK)
     * @param vkUser Карта с данными пользователя VK
     * @return Созданный объект пользователя
     */
    private ClsUser createNewUser(final String login, final Map<?, ?> vkUser) {
        ClsUserDto dto = new ClsUserDto();
        dto.setLogin(login);

        String firstName =
                vkUser.get("first_name") != null ? vkUser.get("first_name").toString() : "";
        String lastName =
                vkUser.get("last_name") != null ? vkUser.get("last_name").toString() : "";
        dto.setName(firstName + " " + lastName);

        dto.setPassword(UUID.randomUUID().toString());

        return userService.registerAndGetEntity(dto);
    }

    /**
     * Генерирует JWT токены для авторизации пользователя.
     * Удаляет существующие refresh токены и создает новый.
     *
     * @param user Объект пользователя
     * @return Ответ с JWT токенами и информацией о пользователе
     */
    private JwtResponse generateAuthTokens(final ClsUser user) {
        log.info("Генерация JWT токенов для пользователя: {} (ID: {})", user.getLogin(), user.getId());

        String jwt = jwtUtils.generateJwtToken(user.getLogin());
        log.debug(
                "JWT токен сгенерирован для пользователя {}: {}",
                user.getLogin(),
                jwt.substring(0, Math.min(20, jwt.length())) + "...");

        refreshTokenService.deleteByUserId(user.getId());
        RegUserToken rt = refreshTokenService.createRefreshToken(user);
        log.debug(
                "Refresh токен создан для пользователя {}: {}",
                user.getLogin(),
                rt.getToken().substring(0, Math.min(20, rt.getToken().length())) + "...");

        JwtResponse response = new JwtResponse(jwt, rt.getToken(), user.getId(), user.getLogin(), user.getName());
        log.info("Токены успешно сгенерированы для пользователя {} (VK ID: {})", user.getLogin(), user.getId());

        return response;
    }

    /**
     * Выполняет авторизацию используя сервисный ключ VK с указанием конкретного пользователя.
     *
     * @param userId ID пользователя ВКонтакте
     * @return Ответ с JWT токенами и информацией о пользователе
     * @throws TokenRefreshException при возникновении ошибок авторизации
     */
    public JwtResponse loginWithVkServiceKeyAndUserId(Long userId) {
        try {
            log.info("Авторизация с использованием сервисного ключа VK для пользователя ID: {}", userId);

            Map<?, ?> userData = callVkApiWithServiceKeyAndUserId(userId);
            String vkId = extractVkUserId(userData);

            if (!vkId.equals(userId.toString())) {
                throw new TokenRefreshException("Несоответствие ID пользователя в ответе VK API");
            }

            String login = VK_USER_PREFIX + vkId;
            ClsUser user = findOrCreateUser(login, userData);

            log.info("Пользователь авторизован через сервисный ключ: {}, VK ID: {}", login, userId);
            return generateAuthTokens(user);
        } catch (Exception e) {
            log.error("Ошибка при авторизации через сервисный ключ VK для пользователя {}", userId, e);
            TokenRefreshException exception = new TokenRefreshException(
                    "Ошибка авторизации через сервисный ключ для пользователя " + userId + ": " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Выполняет запрос к VK API с использованием сервисного ключа для конкретного пользователя.
     *
     * @param userId ID пользователя ВКонтакте
     * @return Данные пользователя
     * @throws TokenRefreshException при ошибке запроса
     */
    private Map<?, ?> callVkApiWithServiceKeyAndUserId(Long userId) {
        try {
            String url = buildVkApiUrlWithUserId(userId);

            log.info("Выполняю запрос к VK API с сервисным ключом для пользователя ID: {}", userId);
            log.debug("URL запроса к VK API: {}", url.replaceAll("access_token=[^&]+", "access_token=***"));

            Map<?, ?> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                throw new TokenRefreshException("Пустой ответ от VK API");
            }

            log.debug("Получен ответ от VK API для пользователя {}: {}", userId, response);

            if (response.containsKey(ERROR_KEY)) {
                Map<?, ?> error = (Map<?, ?>) response.get(ERROR_KEY);
                String errorMsg = error.containsKey(ERROR_MSG_KEY)
                        ? error.get(ERROR_MSG_KEY).toString()
                        : "Неизвестная ошибка";
                log.error("Ошибка VK API для пользователя {}: {}", userId, errorMsg);
                throw new TokenRefreshException("Ошибка VK API: " + errorMsg);
            }

            Map<?, ?> userData = parseVkResponse(response);
            log.info(
                    "Успешно получены данные пользователя VK ID {}: имя={}, фамилия={}",
                    userId,
                    userData.get("first_name"),
                    userData.get("last_name"));

            return userData;
        } catch (Exception e) {
            log.error("Ошибка при запросе к VK API с сервисным ключом для пользователя {}", userId, e);
            TokenRefreshException exception = new TokenRefreshException(
                    "Ошибка запроса к VK API для пользователя " + userId + ": " + e.getMessage());
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Формирует полный URL для запроса к API VK для конкретного пользователя.
     *
     * @param userId ID пользователя ВКонтакте
     * @return Строка с полным URL для запроса
     */
    private String buildVkApiUrlWithUserId(Long userId) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(vkApiUrl)
                .pathSegment(METHOD_PATH, USERS_GET)
                .queryParam(USER_IDS, userId.toString())
                .queryParam(ACCESS_TOKEN, vkServiceKey)
                .queryParam(VERSION, vkApiVersion);

        return builder.toUriString();
    }
}
