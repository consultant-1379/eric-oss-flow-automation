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

FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:${CBOS_VERSION}

ARG CBOS_VERSION
ARG CBO_REPO_URL=https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/${CBOS_VERSION}

ARG COMMIT
ARG BUILD_DATE
ARG APP_VERSION
ARG RSTATE
ARG IMAGE_PRODUCT_NUMBER
LABEL \
    org.opencontainers.image.title=eric-oss-flow-automation-flyway \
    org.opencontainers.image.created=$BUILD_DATE \
    org.opencontainers.image.revision=$COMMIT \
    org.opencontainers.image.vendor=Ericsson \
    org.opencontainers.image.version=$APP_VERSION \
    com.ericsson.product-revision="${RSTATE}" \
    com.ericsson.product-number="$IMAGE_PRODUCT_NUMBER"

RUN zypper ar -C -G -f $CBO_REPO_URL?ssl_verify=no LDC-CBO-SLES \
    && zypper ref -f -r LDC-CBO-SLES \
    && zypper install -l -y curl postgresql13 java-11-openjdk-headless \
    && zypper clean --all

 # Create user for container
RUN echo "218151:x:218151:" >> /etc/group \
    && echo "218151:x:218151:218151:An identity for eric-oss-flow-automation:/home/218151:/bin/false" >> /etc/passwd \
    && echo "218151:!::0:::::" >> /etc/shadow
COPY --chown=218151:218151 flowautomation-service-db/src/main/resources/create_db.sh /home/create_db.sh
COPY --chown=218151:218151 flowautomation-service-db/src/main/resources/entrypoint.sh entrypoint.sh
COPY --chown=218151:218151 scripts/logger.sh /home/logger.sh
RUN chmod +x entrypoint.sh \
    && chmod +x /home/create_db.sh

COPY flowautomation-service-db/target/flowautomation-service-db-jar.jar /home/flowautomation-service-db-jar.jar

USER 218151
CMD ["sh", "./entrypoint.sh"]
