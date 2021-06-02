#Build
FROM maven:3.6.1-jdk-11-slim as maven
COPY ./pom.xml /home/build/pom.xml
COPY ./keycloak-extension /home/build/keycloak-extension

RUN mkdir /home/.m2
WORKDIR /home/.m2
USER root
RUN mvn -f /home/build/pom.xml clean package

#Runtime
FROM jboss/keycloak:11.0.3
COPY --from=maven /home/build/keycloak-extension/target/keycloak-extension-jar-with-dependencies.jar /tmp/keycloak-extension.jar
USER root
RUN mkdir -p /var/cache/{dnf,yum,system-upgrade} && microdnf install findutils -y && microdnf clean all
USER jboss
RUN /opt/jboss/keycloak/bin/jboss-cli.sh --command="module add --name=keycloak.extension --resources=/tmp/keycloak-extension.jar --dependencies=org.keycloak.keycloak-core,org.keycloak.keycloak-services,org.keycloak.keycloak-model-jpa,org.keycloak.keycloak-server-spi,org.keycloak.keycloak-server-spi-private,javax.ws.rs.api,javax.persistence.api,org.hibernate,org.javassist,org.jboss.logging,org.jboss.resteasy.resteasy-jaxrs,org.keycloak.keycloak-common"
RUN sed -i -e 's/classpath:${jboss.home.dir}\/providers\/\*/&\n        <\/provider><provider>module:keycloak.extension/' $JBOSS_HOME/standalone/configuration/standalone-ha.xml

RUN mkdir /tmp/setup/
COPY setup /tmp/setup/

COPY docker-entrypoint.sh /tmp
USER root
RUN chmod 777 /tmp/docker-entrypoint.sh
USER jboss
ENTRYPOINT ["/tmp/docker-entrypoint.sh"]
