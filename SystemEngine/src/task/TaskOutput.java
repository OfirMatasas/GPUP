package task;

import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import summaries.TargetSummary;

import java.io.*;
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
    private Path directoryPath;
    private ArrayList<OutputStream> outputStreams;
    private GraphSummary graphSummary;

    public TaskOutput(String taskType, GraphSummary graphSummary) throws OpeningFileCrash, FileNotFoundException {
//        createNewDirectoryOfTaskLogs(taskType);
//        this.outputStreams = new ArrayList<>();
        this.graphSummary = graphSummary;
//        outputStreams.add(new FileOutputStream(directoryPath.toString()));
//        outputStreams.add(new PrintStream(System.out));
    }

    public void outputStartingTaskOnTarget(String targetName)
    {
        try
        {
            TargetSummary targetSummary = graphSummary.getTargetsSummaryMap().get(targetName);
            Duration time = targetSummary.getPredictedTime();

            String targetExtraInfo, totalTimeFormatted;

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
            System.out.println("Couldn't write to file " + targetName + ".log");
        }
    }

    public void outputEndingTaskOnTarget(String targetName)
    {
        try
        {
            TargetSummary targetSummary = graphSummary.getTargetsSummaryMap().get(targetName);
            Duration time = targetSummary.getTime();
            String totalTimeFormatted, result;

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

//            //Output the new skipped targets after current execution
//            if(!targetSummary.isSkipped() && targetSummary.getResultStatus().equals(TargetSummary.ResultStatus.Failure)
//                    && !targetSummary.getRoot())
//            {
//                for(OutputStream os : outputStreams)
//                    os.write("Targets that have been skipped: ".getBytes(StandardCharsets.UTF_8));
//                for(String skippedTargetName : targetSummary.getSkippedTargets())
//                {
//                    String skippedTargetNameSpaced = skippedTargetName + " ";
//                    for(OutputStream os : outputStreams)
//                        os.write(skippedTargetNameSpaced.getBytes(StandardCharsets.UTF_8));
//                }
//                for(OutputStream os : outputStreams)
//                    os.write(("\n").getBytes(StandardCharsets.UTF_8));
//            }

            for(OutputStream os : outputStreams)
                os.write("------------------------------------------\n".getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            System.out.println("Couldn't write to file " + graphSummary.getTargetsSummaryMap().get(targetName).getTargetName() + ".log");
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
                        outputTargetTaskSummary(currentTarget.getTargetName());
                }
            }
            for(OutputStream os : outputStreams)
                os.write("----------------------------------\n".getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            System.out.println("Couldn't write to " + graphSummary.getGraphName() + ".log");
        }
    }

    public void outputTargetTaskSummary(String targetName)
    {
        try {
            TargetSummary targetSummary = graphSummary.getTargetsSummaryMap().get(targetName);
            Duration time = targetSummary.getTime();

            for(OutputStream os : outputStreams)
                os.write("-----------------------\n".getBytes(StandardCharsets.UTF_8));

            String timeSpentFormatted;
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

    public void createNewDirectoryOfTaskLogs(String taskType) throws OpeningFileCrash {
        directoryPath = Paths.get(graphSummary.getWorkingDirectory());
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
        Date date = new Date();

        directoryPath = Paths.get(directoryPath + taskType + " - " + formatter.format(date).toString());
        try {
            Files.createDirectories(directoryPath);
        } catch (IOException e) {
            throw new OpeningFileCrash(directoryPath);
        }
    }

    public void printStartOfTaskOnGraph(String graphName) {
        System.out.println("Task started on graph " + graphName + "!");
    }

    public Path getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(Path directoryPath) {
        this.directoryPath = directoryPath;
    }

    public ArrayList<OutputStream> getOutputStreams() {
        return outputStreams;
    }

    public void setOutputStreams(ArrayList<OutputStream> outputStreams) {
        this.outputStreams = outputStreams;
    }

    public GraphSummary getGraphSummary() {
        return graphSummary;
    }

    public void setGraphSummary(GraphSummary graphSummary) {
        this.graphSummary = graphSummary;
    }
}
