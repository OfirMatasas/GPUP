package task;

public class WorkerSimulationParameters {
    private final String taskName;
    private final String targetName;
    private final String workerName;
    private final SimulationParameters parameters;

    public WorkerSimulationParameters(String taskName, String targetName, String workerName, SimulationParameters parameters) {
        this.taskName = taskName;
        this.targetName = targetName;
        this.workerName = workerName;
        this.parameters = parameters;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public String getWorkerName() {
        return this.workerName;
    }

    public SimulationParameters getParameters() {
        return this.parameters;
    }
}