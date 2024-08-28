#
# COPYRIGHT Ericsson 2021
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#

ARG CBOS_VERSION=5.15.0-10
#See https://jira-oss.seli.wh.rnd.internal.ericsson.com/browse/IDUN-8050 for updates on how latest images are pulled. Implement when ready.

FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:${CBOS_VERSION} AS builder

ARG CBOS_VERSION
ARG CBO_REPO_URL=https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/${CBOS_VERSION}

RUN zypper ar -C -G -f $CBO_REPO_URL?ssl_verify=no LDC-CBO-SLES \
    && zypper ref -f -r LDC-CBO-SLES \
    && zypper install -l -y unzip \
    && zypper clean --all

  # Create user for container
RUN echo "218151:x:218151:" >> /etc/group \
    && echo "218151:x:218151:218151:An identity for eric-oss-flow-automation:/home/218151:/bin/false" >> /etc/passwd \
    && echo "218151:!::0:::::" >> /etc/shadow

##### JBoss
COPY --chown=218151:218151 flowautomation-service-jboss/target/dependency/jboss-as-dist-7.4.10.zip /tmp
RUN unzip -d / /tmp/jboss-as-dist-7.4.10.zip \
    && mv /jboss-eap-7.4.10 /jboss \
    && rm /tmp/jboss-as-dist-7.4.10.zip

##### Postgres JDBC driver
RUN mkdir -p /jboss/modules/org/postgres/jdbc/jboss/postgres-jboss-subsystem/main
COPY --chown=218151:218151 flowautomation-service-db/src/main/resources/postgresql/ /jboss/modules/org/postgres/jdbc/jboss/postgres-jboss-subsystem/main/

##### Camunda BPM
COPY flowautomation-service-jboss/target/camunda-wars/camunda-engine-rest-*-ee-wildfly.war /jboss/standalone/deployments/
COPY flowautomation-service-jboss/target/camunda-wars/camunda-webapp-ee-jboss-*-ee.war /jboss/standalone/deployments/
COPY flowautomation-service-jboss/target/camunda-modules/ /opt/ericsson/jboss/modules/
RUN chown -R 218151:218151 /opt

#### Camunda FA plugin
RUN mkdir -p /opt/ericsson/jboss/modules/com/ericsson/oss/services/flowautomation/camunda-plugin/main/
COPY flowautomation-service-jboss/src/main/resources/fa/module/ /opt/ericsson/jboss/modules/com/ericsson/oss/services/flowautomation/camunda-plugin/main/
COPY flowautomation-service-camunda-plugin/target/flowautomation-service-camunda-plugin.jar /opt/ericsson/jboss/modules/com/ericsson/oss/services/flowautomation/camunda-plugin/main/
RUN chown -R 218151:218151 /opt

# ... Using 'tr' to do a 'dos2unix' because cannot find dos2unix with zypper install :-(
COPY flowautomation-service-jboss/src/main/resources/camundabpm/install-camundabpm.sh /camundabpm/install-camundabpmx.sh
RUN tr -d '\r' < /camundabpm/install-camundabpmx.sh > /camundabpm/install-camundabpm.sh \
    && chmod +x /camundabpm/install-camundabpm.sh \
    && chown -R 218151:218151 /camundabpm \
    && /camundabpm/install-camundabpm.sh \
    && rm -rf /camundabpm

##### Flow Automation
# ... Using 'tr' to do a 'dos2unix' because cannot find dos2unix with zypper install :-(
COPY flowautomation-service-jboss/src/main/resources/fa/install-fa.sh /fa/install-fax.sh
RUN tr -d '\r' < /fa/install-fax.sh > /fa/install-fa.sh \
    && chmod +x /fa/install-fa.sh \
    && chown -R 218151:218151 /fa \
    && /fa/install-fa.sh \
    && rm -rf /fa

# Copy and own files
COPY --chown=218151:218151 flowautomation-service-jboss/src/main/resources/jboss/standalone.xml /jboss/standalone/configuration/
COPY --chown=218151:218151 flowautomation-service-ear/target/flowautomation-service-ear-*.ear /jboss/standalone/deployments/
COPY --chown=218151:218151 flowautomation-service-jboss/target/fa-ui-war/eric-oss-flow-automation-ui-latest.war /jboss/standalone/deployments/
COPY --chown=218151:218151 flowautomation-service-jboss/src/main/resources/entrypoint.sh entrypoint.sh
RUN chown -R 218151:218151 /jboss

RUN mkdir -p /jboss-runtime && chown -R 218151:218151 /jboss-runtime

# Following changes are done to integrate common-logging library
RUN mkdir -p /FA-tmp \
    && mkdir -p /FA-tmp/module_xml \
    && mkdir -p /FA-tmp/modules/ch/qos/logback/main \
    && mkdir -p /FA-tmp/modules/org \
    && mkdir -p /FA-tmp/modules/org/jboss \
    && mv /jboss/modules/system/layers/base/org/slf4j /FA-tmp/modules/org/ \
    && mv /jboss/modules/system/layers/base/org/jboss/logging /FA-tmp/modules/org/jboss \
    && chown -R 218151:218151 /FA-tmp

COPY --chown=218151:218151 flowautomation-service-jboss/src/main/resources/jboss/modules/common-log-lib_module.xml /FA-tmp/modules/ch/qos/logback/main/module.xml
COPY --chown=218151:218151 flowautomation-service-jboss/src/main/resources/jboss/modules/slf4j_module.xml /FA-tmp/module_xml/
COPY --chown=218151:218151 flowautomation-service-jboss/src/main/resources/jboss/modules/jboss-logging_module.xml /FA-tmp/module_xml/

COPY --chown=218151:218151 flowautomation-service-ear/target/eric-oss-flow-automation-*/lib/com.ericsson.oss.orchestration.eo-eric-common-logging-*.jar /FA-tmp/modules/ch/qos/logback/main/
COPY --chown=218151:218151 flowautomation-service-ear/target/eric-oss-flow-automation-*/lib/ch.qos.logback-logback-classic-*.jar /FA-tmp/modules/ch/qos/logback/main/
COPY --chown=218151:218151 flowautomation-service-ear/target/eric-oss-flow-automation-*/lib/ch.qos.logback-logback-core-*.jar /FA-tmp/modules/ch/qos/logback/main/
COPY --chown=218151:218151 flowautomation-service-ear/target/eric-oss-flow-automation-*/lib/net.logstash.logback-logstash-logback-encoder-*.jar /FA-tmp/modules/ch/qos/logback/main/
COPY --chown=218151:218151 flowautomation-service-ear/target/eric-oss-flow-automation-*/lib/org.apache.httpcomponents.*.jar /FA-tmp/modules/ch/qos/logback/main/
COPY --chown=218151:218151 flowautomation-service-ear/target/eric-oss-flow-automation-*/lib/commons-codec-commons-codec-*.jar /FA-tmp/modules/ch/qos/logback/main/
COPY --chown=218151:218151 flowautomation-service-ear/target/eric-oss-flow-automation-*/lib/com.fasterxml.jackson.core*.jar /FA-tmp/modules/ch/qos/logback/main/

RUN basename /FA-tmp/modules/ch/qos/logback/main/com.ericsson.oss.orchestration.eo-eric-common-logging-*.jar | xargs -I '{}' sed -i 's/{eo-eric-common-logging-jar-file}/{}/g' /FA-tmp/modules/ch/qos/logback/main/module.xml \
    && basename /FA-tmp/modules/ch/qos/logback/main/ch.qos.logback-logback-classic-*.jar | xargs -I '{}' sed -i 's/{logback-classic-jar-file}/{}/g' /FA-tmp/modules/ch/qos/logback/main/module.xml \
    && basename /FA-tmp/modules/ch/qos/logback/main/ch.qos.logback-logback-core-*.jar | xargs -I '{}' sed -i 's/{logback-core-jar-file}/{}/g' /FA-tmp/modules/ch/qos/logback/main/module.xml \
    && basename /FA-tmp/modules/ch/qos/logback/main/net.logstash.logback-logstash-logback-encoder-*.jar | xargs -I '{}' sed -i 's/{logstash-logback-encoder-jar-file}/{}/g' /FA-tmp/modules/ch/qos/logback/main/module.xml \
    && basename /FA-tmp/modules/ch/qos/logback/main/commons-codec-commons-codec-*.jar | xargs -I '{}' sed -i 's/{commons-codec-jar-file}/{}/g' /FA-tmp/modules/ch/qos/logback/main/module.xml \
    && basename /FA-tmp/modules/ch/qos/logback/main/org.apache.httpcomponents.client5-httpclient5-*.jar | xargs -I '{}' sed -i 's/{client5-httpclient5-jar-file}/{}/g' /FA-tmp/modules/ch/qos/logback/main/module.xml \
    && basename /FA-tmp/modules/ch/qos/logback/main/org.apache.httpcomponents.core5-httpcore5-*.jar | xargs -I '{}' sed -i 's/{core5-httpcore5-jar-file}/{}/g' /FA-tmp/modules/ch/qos/logback/main/module.xml \
    && basename /FA-tmp/modules/ch/qos/logback/main/org.apache.httpcomponents.core5-httpcore5-h2-*.jar | xargs -I '{}' sed -i 's/{core5-httpcore5-h2-jar-file}/{}/g' /FA-tmp/modules/ch/qos/logback/main/module.xml \
    && basename /FA-tmp/modules/ch/qos/logback/main/com.fasterxml.jackson.core-jackson-annotations-*.jar | xargs -I '{}' sed -i 's/{core-jackson-annotations-jar-file}/{}/g' /FA-tmp/modules/ch/qos/logback/main/module.xml \
    && basename /FA-tmp/modules/ch/qos/logback/main/com.fasterxml.jackson.core-jackson-core-*.jar | xargs -I '{}' sed -i 's/{core-jackson-core-jar-file}/{}/g' /FA-tmp/modules/ch/qos/logback/main/module.xml \
    && basename /FA-tmp/modules/ch/qos/logback/main/com.fasterxml.jackson.core-jackson-databind-*.jar | xargs -I '{}' sed -i 's/{core-jackson-databind-jar-file}/{}/g' /FA-tmp/modules/ch/qos/logback/main/module.xml

FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:${CBOS_VERSION} AS base

ARG CBOS_VERSION
ARG CBO_REPO_URL=https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/${CBOS_VERSION}

ARG COMMIT
ARG BUILD_DATE
ARG APP_VERSION
ARG RSTATE
ARG PORTHTTP
ARG IMAGE_PRODUCT_NUMBER
LABEL \
    org.opencontainers.image.title=eric-oss-flow-automation-jsb \
    org.opencontainers.image.created=$BUILD_DATE \
    org.opencontainers.image.revision=$COMMIT \
    org.opencontainers.image.vendor=Ericsson \
    org.opencontainers.image.version=$APP_VERSION \
    com.ericsson.product-revision="${RSTATE}" \
    com.ericsson.product-number="$IMAGE_PRODUCT_NUMBER"

RUN zypper ar -C -G -f $CBO_REPO_URL?ssl_verify=no LDC-CBO-SLES \
    && zypper ref -f -r LDC-CBO-SLES \
    && zypper install -l -y java-11-openjdk-headless \
    && zypper clean --all

  # Create user for container
COPY --from=builder /etc/group /etc/group
COPY --from=builder /etc/passwd /etc/passwd
COPY --from=builder /etc/shadow /etc/shadow

# Copy all files from builder image
COPY --chown=218151:218151  --from=builder /jboss /jboss
COPY --chown=218151:218151  --from=builder /opt /opt
COPY --chown=218151:218151  --from=builder /FA-tmp /FA-tmp
COPY --chown=218151:218151  --from=builder /jboss-runtime /jboss-runtime
COPY --chown=218151:218151  --from=builder entrypoint.sh entrypoint.sh

# Entrypoint
CMD ["sh", "./entrypoint.sh"]

USER 218151

EXPOSE 8080

##### To Build the test image with testing dependencies
FROM base AS test

##### Jacoco Agent
COPY flowautomation-service-jboss/src/test/resources/jacoco/org.jacoco.agent-0.8.7-runtime.jar /opt/jacoco-agent.jar