# gradle 好大
FROM gradle:jdk14
WORKDIR /app
COPY * /app/
COPY src /app/src
RUN gradle fatjar --no-daemon
