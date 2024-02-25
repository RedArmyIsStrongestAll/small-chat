# Проект "Small chat"
## Приложение для баров, кафе и прочих заведений камерного характера
##
### Требования:
- java 17
- postgresql 16.1
- rabbitmq 
- rabbitmq_web_stomp plugin
###
### Описание:
        WEB приложение, работающие на HTTP и WS протоколах для общения с клиентом.
    Предосталвяющие анонимный чат (про анонимность см. ниже), имеющий один общий канал
    для общения и множество личных. Пользователь имеет возможность загрузить фотографию и 
    ввести имя, отображаемые другим пользователям.
###
### Аутентификация:
        В соотвествии 149-ФЗ аутентифиация совершается через оператора мобильной связи. 
    В данном WEB приложении это совершается не напрямую, но делегируется сторонними сервисами,
    уже проверенными соотвествующими органыми и функционирующими. Делегировнаие просиходит по
    принципу sso (sing-sign-on). Это также делегирует на эти сервисы выполнения требований
    по сбору, харнению и обработке ПД (152-ФЗ).
        После аутентификации пользовтаеля, создаётся jwt token аутентификации, который используется
    вместо привязки пользовтаеля к сессиям. Время действия токена указывается в внешней переменной
    jwt.live.time.minutes.
        Анонимность приложения имеет характер: 1) легкой смены профиля, 2) отсутвия публичной
    информации о пользователе, 3) не долговечность публичного доступа к сообщениям пользовтаеля.
    Последний пункт предполает частую очистку публичного следа пользовтаеля в приложении, определяемое
    временем указанным в внешней переменной user.live.time.minutes.
###
### Хранение данных:
        Для обеспечения горизонтальной маштобируемости используется RabbitMQ с плагином STOMP.
    Обращение к STOMP плагину через пути /topic и /queue с использованием заголовков обеспечивает
    автоматическое удаление пустых очередей общего и личных чатов и автоматичкую рассылку всем
    подписанным пользовтаелям общего канала. Подробнее в swager в описании websocket мметодов.
        Хранение пользовтаелей, чатов и сообщений происходит в базу данных postgresql. Подробнее
    в файле дампа по пути /database_schema/schema-postgresql.sql
    
###
### Технические особенности:
    - Файлы с переменными окружения лежат в деректории /applications (файл с переменными
    следует расположить в одной деректории с jar исполнчемым файлом)
    - Схема базы данных postgresql лежит по пути /database_schema/schema-postgresql.sql
    - Необходимые конфигурациооные особенности rabbitmq указаны в деректории /rabbitmq_config
    - API swagger можно найти или по адресу /swag при запущенном приложении или 
    в файле swagger_clone/api
    - Файлы лога при работе программы можно найти в деректории log/console 
    в текстовом и json форматах, так же в деректории log/archive при работе программы 
    будут храниться логи за последние 12 суток
    - Метрики для prometheus можно найти по адресу /actuator/prometheus
    
    