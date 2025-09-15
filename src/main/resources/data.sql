-- Инициализация данных для таблицы cls_activation_status
-- Вставка базовых статусов активации пользователей

INSERT INTO public.cls_activation_status (id, name, code, is_deleted) VALUES 
(1, 'Pending', 'PENDING', false),
(2, 'Active', 'ACTIVE', false);
