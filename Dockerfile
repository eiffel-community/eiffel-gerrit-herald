FROM openjdk:12

# Setting eiffel build name
ARG EIFFEL=eiffel-rabbitmq-service-1.0.0.jar

RUN mkdir /app

# Building project
COPY . /temp
WORKDIR temp/
RUN ./gradlew clean jar

# Moving build to app folder
RUN mv build/libs/$EIFFEL /app/$EIFFEL
WORKDIR /app/

# Setting startup command
ENV EIFFEL_ENV $EIFFEL
CMD java -jar $EIFFEL_ENV
