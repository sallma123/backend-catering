# Utiliser une image Java officielle
FROM openjdk:17-jdk-slim

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier jar généré
COPY target/*.jar app.jar

# Exposer le port 8080
EXPOSE 8080

# Lancer l’application
ENTRYPOINT ["java","-jar","app.jar"]
