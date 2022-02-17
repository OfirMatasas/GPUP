package task;

public class CompilationParameters {
    private final String sourceCodeDirectoryPath;
    private final String outputDirectoryPath;

    public CompilationParameters(String sourceCodeDirectoryPath, String outputDirectoryPath) {
        this.sourceCodeDirectoryPath = sourceCodeDirectoryPath;
        this.outputDirectoryPath = outputDirectoryPath;
    }

    public String getSourceCodeDirectoryPath() {
        return this.sourceCodeDirectoryPath;
    }

    public String getOutputDirectoryPath() {
        return this.outputDirectoryPath;
    }
}