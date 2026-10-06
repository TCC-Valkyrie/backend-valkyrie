# O Java deste projeto é o 25

# Docker Banco
docker network create crime-network

cd db

docker build -t mysql-crime .

docker run -d --name mysql-crime -p 3306:3306 mysql-crime

docker exec -it mysql-crime mysql -u crime_user -p

crime_password

USE crime_db;

SHOW TABLES;


# Docker Java

./mvnw package (se tiver q atualizar o java)
./mvnw clean package -DskipTests (se tiver que atualizar o java sem testes)

caso tenha rodado os comando do MVNW pegue o arquivo do target gerado e mova para o OUT/ARTIFACTS/
docker compose up -d --build (SE QUISER DESLIGAR docker compose down)

docker compose logs -f video-service
# Se nao for

docker network connect crime-network crime-mysql

docker network connect crime-network video-service-1

docker network inspect crime-network


# RODANDO EM DOCKER NÃO PRECISA, POREM AO RODAR LOCAL PRECISA DESSA PARADA ffmpeg

https://ffmpeg.org/download.html

Cole isto em uma pasta do seu PC deszip e mude a application.properties q passa o caminho do arquivo para o caminho do seu PC

E nao esqueca de quando gerar o JAR voltar para a sua