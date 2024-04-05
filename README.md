## Описание проекта

### Бот для отслеживания монет на сайтах

Этот проект представляет собой Telegram-бота, который позволяет пользователям отслеживать информацию о различных криптовалютах на веб-сайтах и получать соответствующие уведомления. Бот обладает функционалом регистрации и аутентификации пользователей для персонализированного использования.

### Основной функционал

1. **Регистрация и логин**: Пользователи могут зарегистрироваться в системе и войти в свои учетные записи для доступа к персонализированным функциям бота.
2. **Отслеживание монет**: Пользователи могут добавлять монеты, которые они хотят отслеживать, указав интересующие криптовалюты и сайты для мониторинга.
3. **Получение информации о монетах**: Бот регулярно проверяет указанные сайты на наличие обновлений и уведомляет пользователей о новых данных о монетах.
4. **Уведомления**: Пользователи получают уведомления о новых обновлениях или изменениях в статусе отслеживаемых монет.

### Используемые библиотеки

Для реализации проекта используются следующие библиотеки:

- **Spring Boot Starter Data JPA**: Обеспечивает инструменты для работы с базой данных и хранения информации о пользователях и отслеживаемых монетах.
- **Spring Boot Starter Security**: Предоставляет средства для реализации аутентификации и авторизации пользователей.
- **Spring Boot Starter Web**: Позволяет создавать веб-приложения и взаимодействовать с Telegram API через веб-интерфейс.
- **Telegram Bots Extensions**: Предоставляет расширенные возможности для разработки ботов в Telegram.
- **Jsoup**: Используется для парсинга информации с веб-сайтов.
- **PDFBox и iTextPDF**: Используются для генерации отчетов и уведомлений в формате PDF.
- **Slf4j**: Предоставляет API для обработки логирования в приложении.
