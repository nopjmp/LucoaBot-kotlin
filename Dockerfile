FROM adoptopenjdk/openjdk11-openj9:latest

MAINTAINER Kayla Thompson <jmp@0x.gg>

COPY build/libs/LucoaBot-*.jar /opt/app/app.jar
CMD ["java", "-Xmx128m", "-XX:+IdleTuningGcOnIdle", "-Xtune:virtualized", "-Dfile.encoding=UTF-8", "-jar", "/opt/app/app.jar"]
