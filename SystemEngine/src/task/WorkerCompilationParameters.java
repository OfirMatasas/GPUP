package task;

public class WorkerCompilationParameters {
    private final String taskName;
    private final String targetName;
    private final String workerName;
    private final CompilationParameters parameters;
    private final String targetFQN;

    public WorkerCompilationParameters(String taskName, String targetName, String workerName, CompilationParameters parameters, String targetFQN) {
        this.taskName = taskName;
        this.targetName = targetName;
        this.workerName = workerName;
        this.parameters = parameters;
        this.targetFQN = targetFQN;
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

    public CompilationParameters getParameters() {
        return this.parameters;
    }

    public String getTargetFQN() {
        return this.targetFQN;
    }
}