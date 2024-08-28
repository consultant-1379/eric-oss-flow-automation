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

# This Dockerfile was desgined to create a docker image with Java11, mvn, docker-compose and Docker (dind) installed.
# The use of this image can be done on the CICD pipeline when docker-compsoe is required during the build stage.

ARG CBOS_VERSION=5.15.0-10

FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:${CBOS_VERSION} as builder

# Install an execution evnronment
RUN zypper addrepo -C -G -f https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/${CBOS_VERSION} COMMON_BASE_OS_SLES_REPO && \
    zypper refresh && \
    zypper install -l -y java-11-openjdk-devel

# Install tools
RUN zypper addrepo -C -G -f https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/adp-dev/adp-build-env/1.35.3 CBO_BUILD_REPO && \
    zypper install -l -y docker curl wget gcc make && \
    zypper clean --all

# install Docker in Docker
RUN curl -sSL https://get.docker.com/

#set JAVA_HOME
ENV JAVA_HOME /usr/lib64/jvm/java-11-openjdk-11

# Install Maven.
ARG MAVEN_VERSION=3.6.3
ENV M2_HOME /usr/share/maven
ENV M2 ${M2_HOME}/bin
RUN mkdir ${M2_HOME} | curl -SL \
    https://arm.seli.gic.ericsson.se/artifactory/apache-remote/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    | tar xvz -C ${M2_HOME} --strip-components=1
ENV PATH ${M2_HOME}/bin:$PATH

# set up Ericcson internal certificates
RUN wget http://pki.ericsson.se/CertData/EGADRootCA.crt --directory-prefix=/usr/share/pki/trust/anchors/ &&\
    wget http://pki.ericsson.se/CertData/EGADIssuingCA3.crt --directory-prefix=/usr/share/pki/trust/anchors/ &&\
    openssl x509 -in /usr/share/pki/trust/anchors/EGADRootCA.crt -out /usr/share/pki/trust/anchors/EGADRootCA.pem -outform pem &&\
    openssl x509 -in /usr/share/pki/trust/anchors/EGADIssuingCA3.crt -out /usr/share/pki/trust/anchors/EGADIssuingCA3.pem -outform pem &&\
    rm /usr/share/pki/trust/anchors/EGAD*.crt &&\
    update-ca-certificates -v -f


# Install docker-compose note: version 1.29.2 required as munimum.
# TODO: switch to Ericsson internal installation.
RUN curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
RUN chmod +x /usr/local/bin/docker-compose

# Default behavior: spawn shell
CMD ["/bin/bash"]

USER root