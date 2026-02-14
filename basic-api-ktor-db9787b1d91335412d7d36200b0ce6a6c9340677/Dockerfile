FROM gradle:8.7-jdk17-alpine AS build
#Uso la imagen Alpine para que la imagenn sea ligera y rapida
#AS bueld para el alias

WORKDIR /app 
COPY . .
#El COPY . . para tener el contexto de construcción

RUN gradle build --no-daemon
#Construye el proyecto sin lanzar un demonio

FROM eclipse-temurin:17-jre-alpine
#La imagen de eclipse-temurin:17-jre-alpine solo incluye el JRE, pa lijereza

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
#Copia el Jar que generamos con el build


EXPOSE 8081
#El puerto que usaremos
ENTRYPOINT ["java", "-jar", "app.jar"]
#Ejecuta el app.jar que se copia antes