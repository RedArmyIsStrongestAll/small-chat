FROM postgres:16.1
ENV POSTGRES_USERNAME=postgres
ENV POSTGRES_PASSWORD=postgres

COPY ./dump.sql /docker-entrypoint-initdb.d/initdb.sql
RUN chmod 777 -R /docker-entrypoint-initdb.d && sed -i 's/\r$//' /docker-entrypoint-initdb.d/initdb.sql

EXPOSE 5432