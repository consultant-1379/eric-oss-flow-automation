#!/bin/sh
###########################################################################
# COPYRIGHT Ericsson 2021
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
###########################################################################

_GREP=/bin/grep
_SED=/bin/sed

SCRIPT_NAME="${0}"
LOG_TAG="FLOW_AUTOMATION_INSTALL"

#Jboss Modules Variables
MODULE_CAMUNDA_ENGINE="org.camunda.bpm.camunda-engine"
MODULE_CAMUNDA_JBOSS="org.camunda.bpm.wildfly.camunda-wildfly-subsystem"

DEPENDENCIES="<\/dependencies>"

FA_MODULE="<module name=\"com.ericsson.oss.services.flowautomation.camunda-plugin\" />"

##########################################
#######################################
# Action :
#   Function to update module.xml files.
# Globals:
#   None
# Arguments:
#   $1 Module name
#   $2 Line to be added
# Returns:
#   0 if success
#   1 exit on failure.
#######################################
update_Camunda_Module_With_Plugin() {
    module=$(${_SED} 's/\./\//g' <<< "${1}")
    local file=/opt/ericsson/jboss/modules/${module}/main/module.xml
    count=$($_GREP -c "${2}" $file)
    echo "Module file: $file ${count}"

    # Validate if it does not exist
    if [ $count -eq 0 ]; then
        echo "Update module.xml: ${1} with ${2}"
        "${_SED}" -i.bak "/${DEPENDENCIES}/ i\    ${2}" $file

        # Verifying command execution
        if [ $? -eq 0 ]; then
            echo "File module $file updated successfully"
            return 0;
        else
            echo "Error updating $file module."
            exit 1
        fi
    fi
    return 0;
}

#//////////////////////////
# Script main starts here.
#//////////////////////////

echo "Installing FA"

update_Camunda_Module_With_Plugin "${MODULE_CAMUNDA_ENGINE}" "${FA_MODULE}"

update_Camunda_Module_With_Plugin "${MODULE_CAMUNDA_JBOSS}" "${FA_MODULE}"

echo "FA install completed"

exit 0