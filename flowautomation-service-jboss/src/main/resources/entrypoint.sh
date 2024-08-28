#!/bin/bash
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

mkdir -p /jboss-runtime/deployments

ln -s /jboss/standalone/deployments/*.war /jboss-runtime/deployments
ln -s /jboss/standalone/deployments/*.ear /jboss-runtime/deployments

cp -rf /jboss/standalone/configuration /jboss-runtime/configuration
cp -rf /jboss/standalone/data /jboss-runtime/data

JBOSS_OPT="--server-config=standalone.xml -b 0.0.0.0 -bmanagement 0.0.0.0 -Djboss.server.config.dir=/jboss-runtime/configuration -Djboss.server.data.dir=/jboss-runtime/data -Dorg.apache.james.mime4j.defaultStorageProvider=org.apache.james.mime4j.storage.MemoryStorageProvider"

# controlled injection of modules for integrating logging lib
mkdir -p /jboss-runtime/modules

if [ "$USE_JBOSS_DEFAULT_LOGGING" == "true" ]; then
   ln -s /FA-tmp/modules/org /jboss-runtime/modules/
else

   ln -s /FA-tmp/modules/ch /jboss-runtime/modules/
   cp -rf /FA-tmp/modules/org /jboss-runtime/modules/
   cp -rf /FA-tmp/module_xml/slf4j_module.xml /jboss-runtime/modules/org/slf4j/main/module.xml
   cp -rf /FA-tmp/module_xml/jboss-logging_module.xml /jboss-runtime/modules/org/jboss/logging/main/module.xml

   LOG_FILE_NAME=$(echo $LOGBACK_CONFIG_FILE | cut -d ":" -f 2)
   JBOSS_OPT="${JBOSS_OPT} -Dlogback.configurationFile=${LOG_FILE_NAME} -Dorg.jboss.logging.provider=slf4j"

fi

chown -R 218151:218151 /jboss-runtime/*

/jboss/bin/standalone.sh ${JBOSS_OPT}
