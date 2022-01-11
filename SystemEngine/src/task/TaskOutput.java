package task;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Graph;
import target.Target;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

public class TaskOutput {
    private final TextArea log;
    private final GraphSummary graphSummary;
    private final Graph graph;
    private String directoryPath;

    public TaskOutput(TextArea log, GraphSummary graphSummary, Graph graph) {
        this.log = log;
        this.graphSummary = graphSummary;
        this.graph = graph;
        this.directoryPath = graphSummary.getWorkingDirectory();

        //Clearing the log text area
        this.log.clear();
        this.log.setDisable(false);
    }

    public void printStartOfTaskOnGraph(TaskThread.TaskType taskType) {
        String startingAnnouncement = taskType.toString().substring(0, 1).toUpperCase() + taskType.toString().substring(1) + " task started on graph " + this.graph.getGraphName() + "!\n\n";
        Platform.runLater(() -> this.log.appendText(startingAnnouncement));
    }

    public void createNewDirectoryOfTaskLogs(TaskThread.TaskType taskType) throws OpeningFileCrash {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
        Date date = new Date();

        this.directoryPath += "\\" + taskType.toString() + " - " + formatter.format(date);

        Path path = Paths.get(this.directoryPath);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new OpeningFileCrash(Paths.get(this.directoryPath));
        }
    }

    public void outputStartingCompilationTaskOnTarget(String targetName, CompilationParameters compilationParameters) {
        Target target = this.graph.getTarget(targetName);
        TargetSummary targetSummary = this.graphSummary.getTargetsSummaryMap().get(targetName);
        String userGive = target.getFQN().substring(target.getFQN().indexOf(compilationParameters.getSourceCodeDirectory().getName()) +
                compilationParameters.getSourceCodeDirectory().getName().length() + 1);
        userGive = userGive.replace('.', '\\').concat(".java");
        String toExecute = "javac -d " + compilationParameters.getOutputDirectory().getAbsolutePath() + " -cp " +
                compilationParameters.getOutputDirectory().getAbsolutePath() + " " + userGive;
        String outputString = "Compilation task on target " + targetSummary.getTargetName() + " just started!\n";

        if (targetSummary.getExtraInformation() != null)
            outputString += "Target's extra information: " + targetSummary.getExtraInformation() + "\n";

        outputString += "Task is going to execute : " + toExecute + "\n";

        outputString += "------------------------------------------\n";

        String finalOutputString = outputString;
        Platform.runLater(() -> this.log.appendText(finalOutputString));

        writeToFile(targetName, finalOutputString, true);
    }

    public void outputEndingCompilationTaskOnTarget(String targetName, String failureCause)
    {
        TargetSummary targetSummary = this.graphSummary.getTargetsSummaryMap().get(targetName);
        Duration time = targetSummary.getTime();
        String outputString = "Task on target " + targetSummary.getTargetName() + " ended!\n";

        outputString += "The result: " + targetSummary.getResultStatus().toString() + ".\n";

        if(!Objects.equals(failureCause, ""))
            outputString += "Failure cause: " + failureCause;

        outputString += "Compilation time: " + time.toMillis() + "m/s\n";
        outputString += "------------------------------------------\n";

        String finalOutputString = outputString;
        Platform.runLater(() -> this.log.appendText(finalOutputString));

        writeToFile(targetName, finalOutputString, false);
    }

    private void writeToFile(String targetName, String  output, boolean makeNew)
    {
        Path filePath = Paths.get(this.directoryPath + "\\" + targetName + ".log");

        if(makeNew) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            OutputStream targetFile = new FileOutputStream(filePath.toString(), true);
            targetFile.write(output.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void outputStartingSimulationTaskOnTarget(String targetName, SimulationParameters simulationParameters)
    {
        TargetSummary targetSummary = this.graphSummary.getTargetsSummaryMap().get(targetName);
        String outputString = "Task on target " + targetSummary.getTargetName() + " just started!\n";

        if(targetSummary.getExtraInformation() != null)
            outputString += "Target's extra information: " + targetSummary.getExtraInformation() +"\n";

        outputString += "The system is going to sleep for " + simulationParameters.getProcessingTime().toMillis() + "\n";
        outputString += "------------------------------------------\n";

        String finalOutputString = outputString;
        Platform.runLater(() -> this.log.appendText(finalOutputString));

        writeToFile(targetName, finalOutputString, true);
    }

    public void outputEndingSimulationTaskOnTarget(String targetName)
    {
        TargetSummary targetSummary = this.graphSummary.getTargetsSummaryMap().get(targetName);
        String outputString = "Task on target " + targetSummary.getTargetName() + " ended!\n";

        outputString += "The result: " + targetSummary.getResultStatus().toString() + ".\n";
        outputString += "The system went to sleep for " + targetSummary.getTime().toMillis() + "\n";
        outputString += "------------------------------------------\n";

        String finalOutputString = outputString;
        Platform.runLater(() -> this.log.appendText(finalOutputString));

        writeToFile(targetName, finalOutputString, false);
    }

    public String outputTargetTaskSummary(String targetName) {
        TargetSummary targetSummary = this.graphSummary.getTargetsSummaryMap().get(targetName);
        String targetOutput = "-----------------------\n";

        targetOutput += "Target's name: " + targetName + "\n";
        targetOutput += "Target's result status: ";
        targetOutput += targetSummary.isSkipped() ?  "Skipped\n" : targetSummary.getResultStatus() + "\n";
        targetOutput += !targetSummary.isSkipped() ? "Target's running time: " + targetSummary.getTime().toMillis() + "m/s\n" : "";

        return targetOutput;
    }

    public void outputGraphSummary()
    {
        StringBuilder outputString = new StringBuilder("Graph task summary:\n");
        this.graphSummary.calculateResults();
        Map<TargetSummary.ResultStatus, Integer> results = this.graphSummary.getAllResultStatus();

        outputString.append("Total time spent on task: ").append(this.graphSummary.getTime().toMillis()).append("m/s\n");
        outputString.append("Number of targets succeeded: ").append(results.get(TargetSummary.ResultStatus.Success)).append("\n");
        outputString.append("Number of targets succeeded with warnings: ").append(results.get(TargetSummary.ResultStatus.Warning)).append("\n");
        outputString.append("Number of targets failed: ").append(results.get(TargetSummary.ResultStatus.Failure)).append("\n");
        outputString.append("Number of targets skipped: ").append(this.graphSummary.getSkippedTargets()).append("\n");

        for(TargetSummary currentTarget : this.graphSummary.getTargetsSummaryMap().values())
        {
            if(currentTarget.isRunning())
                outputString.append(outputTargetTaskSummary(currentTarget.getTargetName()));
        }

        outputString.append("---------------------END OF TASK---------------------\n");

        String finalOutputString = outputString.toString();
        Platform.runLater(() -> this.log.appendText(finalOutputString));

        writeToFile("Task Summary", finalOutputString, true);
    }
}
