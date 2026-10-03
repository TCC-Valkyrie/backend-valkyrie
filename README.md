docker build -t mysql-crime .

docker run -d --name mysql-crime -p 3306:3306 mysql-crime

docker exec -it mysql-crime mysql -u crime_user -p

crime_password

USE crime_db;

SHOW TABLES;