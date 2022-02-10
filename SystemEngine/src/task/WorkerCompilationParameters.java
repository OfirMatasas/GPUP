package task;

public class WorkerCompilationParameters {
    private final String taskName;
    private final String targetName;
    private final String workerName;
    private final CompilationParameters parameters;

    public WorkerCompilationParameters(String taskName, String targetName, String workerName, CompilationParameters parameters) {
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

    public CompilationParameters getParameters() {
        return this.parameters;
    }
}