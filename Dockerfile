FROM azul/zulu-openjdk:10

MAINTAINER Kayla Thompson <jmp@0x.gg>

COPY build/libs/LucoaBot-*.jar /app.jar

CMD ["java", "-jar", "/app.jar"]