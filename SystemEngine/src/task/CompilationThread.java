package task;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class CompilationThread implements Runnable
{
    private final String taskName;
    private final String workerName;
    private final String targetName;
    private final CompilationParameters targetParameters;
    private final String FQN;
    private final ExecutedTargetUpdates updates;

    public CompilationThread(WorkerCompilationParameters taskInfo) {
        this.targetParameters = taskInfo.getParameters();
        this.targetName = taskInfo.getTargetName();
        this.workerName = taskInfo.getWorkerName();
        this.taskName = taskInfo.getTaskName();
        this.FQN = taskInfo.getTargetFQN();
        this.updates = new ExecutedTargetUpdates(this.taskName, this.targetName, this.workerName);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.targetName + " Thread");

        File sourceCodeDirectory = new File(this.targetParameters.getSourceCodeDirectoryPath());
        String outputDirectoryPath = this.targetParameters.getOutputDirectoryPath();
        Instant timeStarted;
        Duration workingTime;
        String userGive, resultStatus, errorLog = "", log;

        if(this.FQN.contains(sourceCodeDirectory.getName()))
            userGive = sourceCodeDirectory + "/" + this.FQN.substring(this.FQN.indexOf(sourceCodeDirectory.getName())
                    + sourceCodeDirectory.getName().length() + 1).replace('.','/').concat(".java");
        else
            userGive = (sourceCodeDirectory + "/" + this.FQN).replace('.','/').concat(".java");

        String[] c = {"javac", "-d", outputDirectoryPath, "-cp", outputDirectoryPath, userGive};

        //Starting the clock
        this.updates.taskStarted();
        timeStarted = Instant.now();

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
            workingTime = Duration.between(timeStarted, Instant.now());
            log = "The worker " + this.workerName + " just finished working on target " + this.targetName + "!\n";
            if(Objects.requireNonNull(process).exitValue() != 0) //Failure
            {
                resultStatus = "Failure";
                log +="The result: " + resultStatus + ", cause:\n";
                try {
                    log += new BufferedReader(new InputStreamReader(process.getErrorStream())).readLine() + "\n";
                } catch (IOException e) {
                    e.printStackTrace();
                }
                log += "Total time: " + workingTime.toMillis() + "m/s";
            }
            else //Success
            {
                resultStatus = "Success";
                log += "The result: " + resultStatus + ", total time: " + workingTime.toMillis() + "m/s";
            }

            this.updates.taskFinished(resultStatus, log);
            this.updates.setWorkingTime(workingTime.toMillis());

//            targetSummary.stopTheClock();
//            this.graphSummary.UpdateTargetSummary(this.target, resultStatus, TargetSummary.RuntimeStatus.Finished);
//            this.taskOutput.outputEndingCompilationTaskOnTarget(this.targetName, failureCause);
        }
    }
}
