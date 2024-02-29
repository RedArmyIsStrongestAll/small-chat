# Проект "Small chat"
## Приложение для баров, кафе и прочих заведений камерного характера
##
### Требования:
- java 17
- postgresql 16.1
- rabbitmq, rabbitmq_stomp plugin
- регистрация приложения в VK ID (sso система)
###
### Описание:
        WEB приложение, работающие на HTTP и WS протоколах для общения с клиентом.
    Предосталвяющие анонимный чат (про анонимность см. ниже), имеющий один общий канал
    для общения и множество личных. Пользователь имеет возможность загрузить фотографию и 
    ввести имя, отображаемые другим пользователям.
### Аутентификация:
        В соотвествии 149-ФЗ аутентифиация совершается через оператора мобильной связи. 
    В данном WEB приложении это совершается не напрямую, но делегируется сторонними сервисами,
    уже проверенными соотвествующими органыми и функционирующими. Делегировнаие просиходит по
    принципу sso (sing-sign-on). Это также делегирует на эти сервисы выполнения требований
    по сбору, харнению и обработке ПД (152-ФЗ).
        Для VK ID необходимо зарегестировать свою приложение в id.vk.com в Моих приложениях,
    указав свой домен и получив uuid, app-id и secrets keys (их необходимо указать в внешних переменных
    vk.id.oauth.uuid, vk.id.oauth.app-id, vk.id.oauth.secret-key, vk.id.oauth.service-key).
        Кроме это происходит проверка геолокации, чтобы ограничитьь доступ извне обслуживаемых
    заведений. Множество заведений заносится в внешние переменные по шаблону (они суть множество)
    coordinates.locations.{наименование в одно слово}.latitude и 
    coordinates.locations.{наименование в одно слово}.longitude, что описываю широту и долготу
    нахождения помещения, в переменную radius.location.meters заносится радиус от этих точек.
        После аутентификации пользовтаеля, создаётся jwt token аутентификации, который используется
    вместо привязки пользовтаеля к сессиям. Время действия токена указывается в внешней переменной
    jwt.live.time.minutes.
        Анонимность приложения имеет характер: 1) легкой смены профиля, 2) отсутвия публичной
    информации о пользователе, 3) не долговечность публичного доступа к сообщениям пользовтаеля.
    Последний пункт предполает частую очистку публичного следа пользовтаеля в приложении, определяемое
    временем указанным в внешней переменной user.live.time.minutes.
### Хранение данных:
        Для обеспечения горизонтальной маштобируемости используется RabbitMQ с плагином STOMP.
    Обращение к STOMP плагину через пути /topic и /queue с использованием заголовков обеспечивает
    автоматическое удаление пустых очередей общего и личных чатов и автоматичкую рассылку всем
    подписанным пользовтаелям общего канала. Подробнее в swager в описании websocket мметодов.
        Хранение пользовтаелей, чатов и сообщений происходит в базу данных postgresql. Подробнее
    в файле дампа по пути /database/database_schema/schema-postgresql.sql
### Технические особенности:
    - Файлы с переменными окружения лежат в деректории /backend/applications (файл с переменными
    следует расположить в одной деректории с jar исполнчемым файлом)
    - Схема базы данных postgresql лежит по пути /database/database_schema/schema-postgresql.sql
    - API swagger можно найти или по http адресу /swag при запущенном приложении или 
    в файле /backend/swagger_clone/api
    - Файлы лога при работе программы можно найти в деректории /backend/log/console или в деректории 
    контейнера /log/console в текстовом и json форматах, так же в деректории log/archive при работе
    программы будут храниться логи за последние 12 суток
    - Метрики для prometheus можно найти по http адресу /actuator/prometheus
    - В деректориях /backend, /database, /broker представлены Dockerfile's для самого приложения java,
    базы данных postgres и брокера rabbitmq
    