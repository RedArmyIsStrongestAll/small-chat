FROM rabbitmq:3.12.13-management
RUN rabbitmq-plugins enable rabbitmq_stomp
COPY ./rabbitmq.conf /etc/rabbitmq/rabbitmq.conf
RUN chmod 777 /etc/rabbitmq/rabbitmq.conf

EXPOSE 61613
EXPOSE 15672

