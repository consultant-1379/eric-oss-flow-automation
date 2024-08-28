import com.ericsson.oss.services.flowautomation.flowapi.Reporter

Reporter.updateReportSummary(execution, "Starting Connectivity Simulation...")

List<String> neNames = new ArrayList<>()
def nodes = flowInput.selectedNodesTask.selectedNodes
nodes.each { node ->
    neNames.add(node.neName)
}
Reporter.updateReportVariable(execution, "nodesSelected", neNames.join(","))

def relationsSelected = []
def relationsTable = flowInput.relationChoice.relationsTable
def dateTimeValue = flowInput.additionalSettings.simDateTime

if (relationsTable != null) {
    relationsTable.each {
        def index = it.node1 + ":" + it.node2
        relationsSelected = relationsSelected + [index]

        def nodeResult = [:]
        nodeResult.put("node1", it.node1)
        nodeResult.put("node2", it.node2)
        nodeResult.put("connectivity", 0)
        nodeResult.put("accuracy", ["status": "notStarted"])
        nodeResult.put("simDateTime", dateTimeValue)

        Reporter.updateReportVariable(execution, index + "_summary", nodeResult)
    }
    Reporter.updateReportVariable(execution, "relationsSelected", relationsSelected.join(","))
}

execution.setVariable("numberOfSimulations", 0)
