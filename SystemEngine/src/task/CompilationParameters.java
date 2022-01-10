package task;

import java.io.File;

public class CompilationParameters {
    private final File sourceCodeDirectory;
    private final File outputDirectory;

    public CompilationParameters(File sourceCodeDirectory, File outputDirectory) {
        this.sourceCodeDirectory = sourceCodeDirectory;
        this.outputDirectory = outputDirectory;
    }

    public File getSourceCodeDirectory() {
        return sourceCodeDirectory;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }
}
