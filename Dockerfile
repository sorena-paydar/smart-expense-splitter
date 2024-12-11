# Use Amazon Corretto 21 as the base image
FROM amazoncorretto:21

# Set the working directory in the container
WORKDIR /app

# Copy the application JAR file from the target folder
COPY build/libs/SmartExpenseSplitter-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port (adjust as necessary)
EXPOSE 8080

# Run the application using Amazon Corretto
ENTRYPOINT ["java", "-jar", "app.jar"]

