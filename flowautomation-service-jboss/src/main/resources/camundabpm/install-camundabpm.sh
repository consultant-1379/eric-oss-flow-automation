#!/bin/bash
###########################################################################
# COPYRIGHT Ericsson 2021
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
###########################################################################

#Standard linux commands
_GREP=/bin/grep
_SED=/bin/sed

#For logging
LOG_TAG="CAMUNDA_JBOSS"
SCRIPT_NAME="${0}"

MODULE_GROOVY="org.codehaus.groovy.groovy-all"
DEPENDENCIES="<\/dependencies>"
SUN_JDK_DEPENDENCY="<module name=\"sun.jdk\" services=\"import\" />"

#######################################
# Action :
#   Function to update module.xml files.
# Globals:
#   _SED
#   _GREP
#   $DEPENDENCIES
# Arguments:
#   $1 Module name
#   $2 dependency to be added
# Returns:
#   0 if success
#   1 exit on failure.
#######################################
updateModule() {
    local module_path
    module_path=$($_SED 's/\./\//g' <<< "${1}")

    local module_xml=/opt/ericsson/jboss/modules/${module_path}/main/module.xml
    local dependency="${2}"

    if ! $_GREP -q "${dependency}" "${module_xml}"; then
        if $_SED -i "/$DEPENDENCIES/ i\    ${dependency}" "${module_xml}"; then
            echo "File ${module_xml} updated successfully"
            return 0;
        else
            echo "Error updating ${module_xml} file."
            exit 1
        fi
    fi
    return 0;
}

#////////////////////////////////
# Script main starts here.
#////////////////////////////////

echo "Installing Camunda BPM"

updateModule "$MODULE_GROOVY" "$SUN_JDK_DEPENDENCY"

echo "Camunda BPM install completed"

exit 0
