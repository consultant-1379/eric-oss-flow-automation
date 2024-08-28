/*******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/


import org.slf4j.LoggerFactory

import java.text.CharacterIterator
import java.text.SimpleDateFormat
import java.text.StringCharacterIterator

import groovy.json.JsonSlurperClassic

import com.ericsson.oss.services.flowautomation.flowapi.Reporter

def logger = LoggerFactory.getLogger(this.class)
def stdOut = new StringBuilder()
def stdErr = new StringBuilder()
def reindexPath = execution.getVariable("reindexPath")
def proc = reindexPath.execute()

proc.consumeProcessOutput(stdOut, stdErr)
proc.waitForOrKill(60000)

def jsonOutput = {}
def failed = false
def failedMessage = ""
if (!stdErr.toString().equals("")) {
    failed = true
    failedMessage = "An error has occurred while executing REINDEX of TABLE act_hi_actinst: " + stdErr.toString()
} else if (stdOut.toString().equals("")) {
    failed = true
    failedMessage = "REINDEX of TABLE act_hi_actinst took too long to execute. The output of the script, if any, has not been collected."
} else {
    jsonOutput = new JsonSlurperClassic().parseText(stdOut.toString())
    if (jsonOutput == null || jsonOutput.scriptExecutionOutput == null) {
        failed = true
        failedMessage = "An error has occurred while executing REINDEX of TABLE act_hi_actinst: " + stdOut.toString()
    }
}

Map eventData = new HashMap()
eventData.put("EventType","REINDEX")

if (failed) {
    if (execution.getVariable("REINDEX_RETRY_COUNTER") >= 3) {
        execution.setVariable("REINDEX_DONE", true)
    } else {
        eventData.put("OverallResult", failedMessage + " RETRYING...")
        execution.setVariable("REINDEX_RETRY_COUNTER", execution.getVariable("REINDEX_RETRY_COUNTER") + 1)
    }
} else {
    execution.setVariable("REINDEX_DONE", true)
}

if (execution.getVariable("REINDEX_DONE")) {
    def date = new Date()
    def dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date)
    def index = new SimpleDateFormat("yyyyMMddHHmmss").format(date)
    def reindexDateTimes = execution.getVariable("reindexDateTimes")
    if (reindexDateTimes == null) {
        reindexDateTimes = []
    }
    reindexDateTimes = [index] + reindexDateTimes
    if (reindexDateTimes.size() > 7) {
        reindexDateTimes = reindexDateTimes.take(7)
    }
    execution.setVariable("reindexDateTimes", reindexDateTimes)
    Reporter.updateReportVariable(execution, "reindexDateTimes", reindexDateTimes.join(","))

    Reporter.updateReportVariable(execution, index + ".dateTime", dateTime)

    if (failed) {
        eventData.put("OverallResult", stdErr.toString())
        Reporter.updateReportVariable(execution, index + ".scriptExecutionOutput", failedMessage)
    } else {
        Reporter.updateReportVariable(execution, index + ".scriptExecutionOutput", jsonOutput.scriptExecutionOutput)
        eventData.put("OverallResult", jsonOutput.scriptExecutionOutput)
        if (jsonOutput.reindexExecutionDuration != null) {
            Reporter.updateReportVariable(execution, index + ".reindexExecutionDuration", jsonOutput.reindexExecutionDuration)
        }
        if (jsonOutput.tableInitialSize != null) {
            Reporter.updateReportVariable(execution, index + ".tableInitialSize", convertBytesToMegaBytes(jsonOutput.tableInitialSize))
        }
        if (jsonOutput.tableFinalSize != null) {
            Reporter.updateReportVariable(execution, index + ".tableFinalSize", convertBytesToMegaBytes(jsonOutput.tableFinalSize))
        }
    }
    logger.info("Recording FLOW_AUTOMATION_FLOWS. EventData: {}", eventData)
}

Double convertBytesToMegaBytes(Long bytesLong) {
    return Double.valueOf(String.format("%.2f", bytesLong.doubleValue() / (1024.0 * 1024.0)))
}