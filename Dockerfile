FROM java:8

ADD /target/kbs-0.0.1-SNAPSHOT.war kbs.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "kbs.war"]







