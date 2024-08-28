import com.ericsson.oss.services.flowautomation.flowapi.usertask.UsertaskInputProcessingError

def fileContentsText = fileUploadTask.fileSelector.value.text
fileContentsText = fileContentsText.replaceAll("\n", "")
fileContentsText = fileContentsText.replaceAll("\r", "")
fileContentsText = fileContentsText.replaceAll("\t", "")
List<String> nodes
try {
  nodes = Arrays.asList(fileContentsText.split("\\s*,\\s*"))
  if (nodes == null || nodes.isEmpty() || nodes.size() < 2) {
    throw new UsertaskInputProcessingError("error.fa.usertask.input.invalid", "The file should contain a comma separated list of node names and types (i.e.: NodeName1=NodeType1,NodeName2=NodeType2,NodeName3=NodeType3)");
  }
} catch (Exception e) {
  throw new UsertaskInputProcessingError("error.fa.usertask.input.invalid", "The file should contain a comma separated list of node names and types (i.e.: NodeName1=NodeType1,NodeName2=NodeType2,NodeName3=NodeType3)");
}

flowInput.fileUploadTask = fileUploadTask

List<String> nodeNames = new ArrayList<>()
List<String> nodeTypes = new ArrayList<>()
for (int i = 0; i < nodes.size(); i++) {
  if (nodes.get(i).contains("=")) {
    String[] splitted = nodes.get(i).split("=")
    nodeNames.add(splitted[0])
    nodeTypes.add(splitted[1])
  } else {
    nodeNames.add(nodes.get(i))
    nodeTypes.add("None")
  }
}
execution.setVariable("neNames", nodeNames)
execution.setVariable("neTypes", nodeTypes)
