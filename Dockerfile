FROM store/oracle/jdk:11

LABEL maintainer="Arpit Gupta <gupta.arpit03@gmail.com>"

RUN mkdir /usr/app

COPY target/massmailer-jar-with-dependencies.jar /usr/app/

WORKDIR /usr/app

ENTRYPOINT ["java", "-jar", "massmailer-jar-with-dependencies.jar"]

CMD tail -f /dev/null