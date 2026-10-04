#banco
docker network create crime-network

cd db

docker build -t mysql-crime .

docker run -d --name mysql-crime -p 3306:3306 mysql-crime

docker exec -it mysql-crime mysql -u crime_user -p

crime_password

USE crime_db;

SHOW TABLES;


# java

./mvnw package (se tiver q atualizar o java)
./mvnw clean package -DskipTests (se tiver que atualizar o java sem testes)
docker compose up -d --build

docker compose logs -f video-service
# se nao for

docker network connect crime-network crime-mysql

docker network connect crime-network video-service-1

docker network inspect crime-network

