import com.ericsson.oss.services.flowautomation.flowapi.Reporter

Reporter.updateReportSummary(execution, "Simulation Complete")

def r = new Random()
def list = []
def relationsTable = flowInput.relationChoice.relationsTable
def dateTimeValue = flowInput.additionalSettings.simDateTime

if (relationsTable != null) {
    int count = 0
    relationsTable.each {
        def index = it.node1 + ":" + it.node2
        def connectivity = 2 + r.nextInt(15)
        def accuracy = null
        if (count % 5 == 0) {
            accuracy = [status: "inProgress", value: [percent: 55 + r.nextInt(35)]]
        } else if (count % 5 == 1) {
            accuracy = [status: "success", value: [percent: 100]]
        } else if (count % 5 == 2) {
            accuracy = [status: "error", value: [percent: 45 - r.nextInt(35)]]
        } else if (count % 5 == 3) {
            accuracy = [status: "notStarted"]
        }
        list.add("node1": it.node1, "node2": it.node2, "connectivity": connectivity, "cmSyncStatus": "SYNCHRONIZED")

        def nodeResult = [:]
        nodeResult.put(index + ".node1", it.node1)
        nodeResult.put(index + ".node2", it.node2)
        nodeResult.put(index + ".connectivity", connectivity)
        if (accuracy != null) {
            nodeResult.put(index + ".accuracy", accuracy)
        }
        nodeResult.put(index + ".cmSyncStatus", "SYNCHRONIZED")
        nodeResult.put("simDateTime", dateTimeValue)

        Reporter.updateReportVariable(execution, index + "_summary", nodeResult)
        count++
    }
}

execution.setVariable("connectivityTableMap", list)

def numberOfSimulations = execution.getVariable("numberOfSimulations") + 1
Reporter.updateReportVariable(execution, "numberOfSimulations", numberOfSimulations)
execution.setVariable("numberOfSimulations", numberOfSimulations)
def networkTraffic = [status: "inProgress", value: [percent: 10 + r.nextInt(80)]]
Reporter.updateReportVariable(execution, "networkTraffic", networkTraffic)
