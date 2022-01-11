package task;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Target;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Objects;

public class CompilationThread implements Runnable
{
    private final GraphSummary graphSummary;
    private final TextArea log;
    private final Target target;
    private final String targetName;
    private final CompilationParameters compilationParameters;

    public CompilationThread(Target target, GraphSummary graphSummary, TextArea log, CompilationParameters compilationParameters) {

        this.graphSummary = graphSummary;
        this.target = target;
        this.targetName = target.getTargetName();
        this.log = log;
        this.compilationParameters = compilationParameters;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.targetName + " Thread");
        TargetSummary targetSummary = this.graphSummary.getTargetsSummaryMap().get(this.targetName);
        TargetSummary.ResultStatus resultStatus;
        File sourceCodeDirectory = this.compilationParameters.getSourceCodeDirectory();
        String outputDirectoryPath = this.compilationParameters.getOutputDirectory().getAbsolutePath();
        String FQN = this.target.getFQN();
        String userGive;

        if(FQN.contains(sourceCodeDirectory.getName()))
            userGive = sourceCodeDirectory + "/" + FQN.substring(FQN.indexOf(sourceCodeDirectory.getName())
                    + sourceCodeDirectory.getName().length() + 1).replace('.','/').concat(".java");
        else
            userGive = (sourceCodeDirectory + "/" + FQN).replace('.','/').concat(".java");

        String[] c = {"javac", "-d", outputDirectoryPath, "-cp", outputDirectoryPath, userGive};
        String failureCause = "";

        //Starting the clock
        targetSummary.startTheClock();
        outputStartingTaskOnTarget(targetSummary, this.log, c);
        this.graphSummary.UpdateTargetSummary(this.target, TargetSummary.ResultStatus.Undefined, TargetSummary.RuntimeStatus.InProcess, true);

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(c);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            try {
                process = Runtime.getRuntime().exec(c);
                process.waitFor();
            } catch (IOException | InterruptedException ignored) {
            }
        }
        finally {
            if(Objects.requireNonNull(process).exitValue() != 0) //Failure
            {
                resultStatus = TargetSummary.ResultStatus.Failure;
                try {
                    failureCause = new BufferedReader(new InputStreamReader(process.getErrorStream())).readLine() + "\n";
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else //Success
                resultStatus = TargetSummary.ResultStatus.Success;

            targetSummary.stopTheClock();
            this.graphSummary.UpdateTargetSummary(this.target, resultStatus, TargetSummary.RuntimeStatus.Finished, false);
            outputEndingTaskOnTarget(targetSummary, failureCause);
        }
    }

    public void outputStartingTaskOnTarget(TargetSummary targetSummary, TextArea log, String[] c)
    {
        String userGive = this.target.getFQN().substring(target.getFQN().indexOf(this.compilationParameters.getSourceCodeDirectory().getName()) +
                this.compilationParameters.getSourceCodeDirectory().getName().length() +1);
        userGive = userGive.replace('.','\\').concat(".java");
        String toExecute = "javac -d " + this.compilationParameters.getOutputDirectory().getAbsolutePath() + " -cp " +
                this.compilationParameters.getOutputDirectory().getAbsolutePath() + " " + userGive;
        String outputString = "Compilation task on target " + targetSummary.getTargetName() + " just started!\n";

        if(targetSummary.getExtraInformation() != null)
            outputString += "Target's extra information: " + targetSummary.getExtraInformation() +"\n";

        outputString+= "Task is going to execute : " + toExecute +"\n";

        outputString += "------------------------------------------\n";

        String finalOutputString = outputString;
        Platform.runLater(() -> System.out.println(finalOutputString));
        Platform.runLater(() -> this.log.appendText(finalOutputString));
    }

    public void outputEndingTaskOnTarget(TargetSummary targetSummary, String failureCause)
    {
        Duration time = targetSummary.getTime();
        String outputString = "Task on target " + targetSummary.getTargetName() + " ended!\n";

        outputString += "The result: " + targetSummary.getResultStatus().toString() + ".\n";

        if(!Objects.equals(failureCause, ""))
            outputString += "Failure cause: " + failureCause;

        outputString += "Compilation time: " + time.toMillis() + "m/s\n";
        outputString += "------------------------------------------\n";

        String finalOutputString = outputString;
        Platform.runLater(() -> System.out.println(finalOutputString));
        Platform.runLater(() -> this.log.appendText(finalOutputString));
    }
}
