package task;

import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import summaries.TargetSummary;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class TaskOutput
{
    private String directoryPath;
    private ArrayList<OutputStream> outputStreams;
    private TargetSummary targetSummary;
    private GraphSummary graphSummary;

    public TaskOutput(String directoryPath, ArrayList<OutputStream> outputStreams, TargetSummary targetSummary, GraphSummary graphSummary) {
        this.directoryPath = directoryPath;
        this.outputStreams = outputStreams;
        this.targetSummary = targetSummary;
        this.graphSummary = graphSummary;
    }

    public void outputStartingTaskOnTarget()
    {
        try
        {
            Duration time = targetSummary.getPredictedTime();

            String targetName, targetExtraInfo, totalTimeFormatted;

            targetName = "Task on target " + targetSummary.getTargetName() + " just started.\r\n";
            for(OutputStream os : outputStreams)
                os.write(targetName.getBytes(StandardCharsets.UTF_8));

            if(targetSummary.getExtraInformation() != null)
            {
                targetExtraInfo = "Target's extra information: " + targetSummary.getExtraInformation() +"\n";
                for(OutputStream os : outputStreams)
                    os.write(targetExtraInfo.getBytes(StandardCharsets.UTF_8));
            }

            totalTimeFormatted = String.format("The system is going to sleep for %02d:%02d:%02d\n",
                    time.toHours(), time.toMinutes(), time.getSeconds());
            for(OutputStream os : outputStreams)
                os.write(totalTimeFormatted.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            System.out.println("Couldn't write to file " + targetSummary.getTargetName() + ".log");
        }
    }

    public void outputEndingTaskOnTarget()
    {
        try
        {
            Duration time = targetSummary.getTime();
            String targetName, totalTimeFormatted, result;

            targetName = "Task on target " + targetSummary.getTargetName() + " ended.\n";
            for(OutputStream os : outputStreams)
                os.write(targetName.getBytes(StandardCharsets.UTF_8));

            totalTimeFormatted = String.format("The system went to sleep for %02d:%02d:%02d\n",
                    time.toHours(), time.toMinutes(), time.getSeconds());
            for(OutputStream os : outputStreams)
                os.write(totalTimeFormatted.getBytes(StandardCharsets.UTF_8));

            result = "The result: " + targetSummary.getResultStatus().toString() + ".\n";
            for(OutputStream os : outputStreams)
                os.write(result.getBytes(StandardCharsets.UTF_8));

            //Output the new opened targets (might be executable) after current execution
            if(!targetSummary.getOpenedTargets().isEmpty())
            {
                for(OutputStream os : outputStreams)
                    os.write("Targets that have been opened for execution: ".getBytes(StandardCharsets.UTF_8));
                for(String openedTargetName : targetSummary.getOpenedTargets())
                {
                    String openedTargetNameSpaced = openedTargetName + " ";
                    for(OutputStream os : outputStreams)
                        os.write(openedTargetNameSpaced.getBytes(StandardCharsets.UTF_8));
                }
                for(OutputStream os : outputStreams)
                    os.write(("\n").getBytes(StandardCharsets.UTF_8));
            }

            //Output the new skipped targets after current execution
            if(!targetSummary.isSkipped() && targetSummary.getResultStatus().equals(TargetSummary.ResultStatus.Failure)
                    && !targetSummary.getRoot())
            {
                for(OutputStream os : outputStreams)
                    os.write("Targets that have been skipped: ".getBytes(StandardCharsets.UTF_8));
                for(String skippedTargetName : targetSummary.getSkippedTargets())
                {
                    String skippedTargetNameSpaced = skippedTargetName + " ";
                    for(OutputStream os : outputStreams)
                        os.write(skippedTargetNameSpaced.getBytes(StandardCharsets.UTF_8));
                }
                for(OutputStream os : outputStreams)
                    os.write(("\n").getBytes(StandardCharsets.UTF_8));
            }

            for(OutputStream os : outputStreams)
                os.write("------------------------------------------\n".getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            System.out.println("Couldn't write to file " + targetSummary.getTargetName() + ".log");
        }
    }

    public void outputGraphSummary()
    {
        try
        {
            Duration time = graphSummary.getTime();
            for(OutputStream os : outputStreams)
                os.write("Graph task summary:\n".getBytes(StandardCharsets.UTF_8));

            String timeSpentFormatted = String.format("Total time spent on task: %02d:%02d:%02d\n",
                    time.toHours(), time.toMinutes(), time.getSeconds());
            for(OutputStream os : outputStreams)
                os.write(timeSpentFormatted.getBytes(StandardCharsets.UTF_8));

            Map<TargetSummary.ResultStatus, Integer> results = graphSummary.getAllResultStatus();
            String succeeded, warnings, failed, skipped;

            succeeded = "Number of targets succeeded: " + results.get(TargetSummary.ResultStatus.Success) + "\n";
            for(OutputStream os : outputStreams)
                os.write(succeeded.getBytes(StandardCharsets.UTF_8));

            warnings = "Number of targets succeeded with warnings: " + results.get(TargetSummary.ResultStatus.Warning) + "\n";
            for(OutputStream os : outputStreams)
                os.write(warnings.getBytes(StandardCharsets.UTF_8));

            failed = "Number of targets failed: " + results.get(TargetSummary.ResultStatus.Failure) + "\n";
            for(OutputStream os : outputStreams)
                os.write(failed.getBytes(StandardCharsets.UTF_8));

            skipped = "Number of targets skipped: " + graphSummary.getSkippedTargets() + "\n";
            for(OutputStream os : outputStreams)
                os.write(skipped.getBytes(StandardCharsets.UTF_8));

            for(TargetSummary currentTarget : graphSummary.getTargetsSummaryMap().values())
            {
                if(currentTarget.isRunning())
                {
                    for(OutputStream os : outputStreams)
                        outputTargetTaskSummary(currentTarget);
                }
            }
            for(OutputStream os : outputStreams)
                os.write("----------------------------------\n".getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            System.out.println("Couldn't write to " + graphSummary.getGraphName() + ".log");
        }
    }

    public void outputTargetTaskSummary(TargetSummary targetSummary)
    {
        try {
            Duration time = targetSummary.getTime();

            for(OutputStream os : outputStreams)
                os.write("-----------------------\n".getBytes(StandardCharsets.UTF_8));

            String targetName, timeSpentFormatted;
            String result = "Target's result status: ";

            targetName = "Target's name :" + targetSummary.getTargetName() + "\n";
            for(OutputStream os : outputStreams)
                os.write(targetName.getBytes(StandardCharsets.UTF_8));

            if(targetSummary.isSkipped())
                result += "Skipped\n";
            else
                result += targetSummary.getResultStatus() + "\n";

            for(OutputStream os : outputStreams)
                os.write(result.getBytes(StandardCharsets.UTF_8));

            if(!targetSummary.isSkipped())
            {
                timeSpentFormatted = String.format("Target's running time: %02d:%02d:%02d\n", time.toHours(), time.toMinutes(), time.getSeconds());

                for(OutputStream os : outputStreams)
                    os.write(timeSpentFormatted.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public Path createNewDirectoryOfTaskLogs(String taskName, Path workingDirectory) throws OpeningFileCrash {
        directoryPath = workingDirectory.toString();
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
        Date date = new Date();

        directoryPath += "\\" + taskName + " - " + formatter.format(date).toString();

        Path path = Paths.get(directoryPath);
        try {
            Files.createDirectories(path);
            return path;
        } catch (IOException e) {
            throw new OpeningFileCrash(directoryPath);
        }
    }

    public void printStartOfTaskOnGraph(String graphName) {
        System.out.println("Task started on graph " + graphName + "!");
    }
}
