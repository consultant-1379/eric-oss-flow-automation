#
# COPYRIGHT Ericsson 2022
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

# The use of this image can be done on the CICD pipeline when the swagger coverage tool is required during the build stage.
ARG CBOS_VERSION=5.15.0-10

FROM armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles:${CBOS_VERSION} as builder

  # Install an execution environment
RUN zypper addrepo -C -G -f https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/${CBOS_VERSION} COMMON_BASE_OS_SLES_REPO && \
zypper refresh && \
zypper install -l -y java-11-openjdk-devel

  # Install tools
RUN zypper addrepo -C -G -f https://arm.sero.gic.ericsson.se/artifactory/proj-ldc-repo-rpm-local/adp-dev/adp-build-env/1.35.3 CBO_BUILD_REPO && \
zypper install -l -y wget unzip && \
zypper clean --all

  #set JAVA_HOME
ENV JAVA_HOME /usr/lib64/jvm/java-11-openjdk-11

  # Install swagger coverage tool
RUN wget https://github.com/viclovsky/swagger-coverage/releases/download/1.5.0/swagger-coverage-1.5.0.zip -P ./usr/local/bin/
RUN unzip ./usr/local/bin/swagger-coverage-1.5.0.zip

ENV PATH=$PATH:/swagger-coverage-commandline-1.5.0/bin/


  # Default behavior: spawn shell
CMD ["/bin/bash"]

USER root