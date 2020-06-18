FROM openjdk:12 AS build

COPY . /temp
WORKDIR temp
RUN ./gradlew clean jar

FROM openjdk:12

# Use the shell form of ENTRYPOINT to allow environment variable expansion.
# exec is used to make sure that the JVM gets pid 1 so that it receives
# SIGTERM et al and gets an opportunity to shut down cleanly.
ENTRYPOINT exec java ${JAVA_OPTS:-} -jar /app/eiffel-gerrit-herald.jar ${HERALD_OPTS:-}

RUN mkdir /app
WORKDIR /app

COPY --from=build /temp/build/libs/eiffel-gerrit-herald-*.jar /app/eiffel-gerrit-herald.jar
