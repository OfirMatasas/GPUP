package dtos;

import java.util.Map;

public class DashboardGraphDTO {
    private String graphName;
    private String uploaderName;
    private Map<String, Integer> targets;
    private Map<String, Integer> tasksPrices;

    public String getGraphName() {
        return this.graphName;
    }

    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public String getUploaderName() {
        return this.uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public Map<String, Integer> getTargets() {
        return this.targets;
    }

    public void setTargets(Map<String, Integer> targets) {
        this.targets = targets;
    }

    public Map<String, Integer> getTasksPrices() {
        return this.tasksPrices;
    }

    public void setTasksPrices(Map<String, Integer> tasksPrices) {
        this.tasksPrices = tasksPrices;
    }
}
