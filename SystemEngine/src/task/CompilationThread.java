package task;

import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Target;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class CompilationThread implements Runnable
{
    private final GraphSummary graphSummary;
    private final Target target;
    private final String targetName;
    private final CompilationParameters compilationParameters;
    private final TaskOutput taskOutput;

    public CompilationThread(Target target, GraphSummary graphSummary, CompilationParameters compilationParameters, TaskOutput taskOutput) {

        this.graphSummary = graphSummary;
        this.target = target;
        this.targetName = target.getTargetName();
        this.compilationParameters = compilationParameters;
        this.taskOutput = taskOutput;
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
        this.taskOutput.outputStartingCompilationTaskOnTarget(this.targetName, this.compilationParameters);
        this.graphSummary.UpdateTargetSummary(this.target, TargetSummary.ResultStatus.Undefined, TargetSummary.RuntimeStatus.InProcess);

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
            this.graphSummary.UpdateTargetSummary(this.target, resultStatus, TargetSummary.RuntimeStatus.Finished);
            this.taskOutput.outputEndingCompilationTaskOnTarget(this.targetName, failureCause);
        }
    }
}
