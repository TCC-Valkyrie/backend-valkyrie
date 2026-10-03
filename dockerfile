FROM mysql:8.4

ENV MYSQL_ROOT_PASSWORD=root
ENV MYSQL_DATABASE=crime_db
ENV MYSQL_USER=crime_user
ENV MYSQL_PASSWORD=crime_password

COPY 01-init.sql /docker-entrypoint-initdb.d/01-init.sql