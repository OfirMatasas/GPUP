package task;

import myExceptions.FileNotFound;
import myExceptions.OpeningFileCrash;
import summaries.GraphSummary;
import summaries.TargetSummary;
import target.Graph;
import target.Target;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class SimulationThread extends TaskKind {
    private TaskParameters targetParameters;
    private final Graph graph;
    private final GraphSummary graphSummary;
    private final TaskOutput taskOutput;
    private Path filePath;
    private final String targetName;

    public SimulationThread(TaskParameters targetParameters, Graph graph, GraphSummary graphSummary,
                            String targetName, TaskOutput taskOutput) throws FileNotFoundException, FileNotFound, IOException, OpeningFileCrash {
        this.targetParameters = targetParameters;
        this.graph = graph;
        this.graphSummary = graphSummary;
        this.targetName = targetName;
        this.taskOutput = taskOutput;
        this.filePath = Paths.get(taskOutput.getDirectoryPath() + "/" + targetName + ".log");
    }

    @Override
    public void run() {
        //Make a set of executable targets
        Target currentTarget;

        //Creating a file for target
        filePath = Paths.get(output + "\\" + currentTarget.getTargetName() + ".log");
        try {
            Files.createFile(filePath);
        } catch (IOException e) {
            throw new OpeningFileCrash(filePath.getFileName().toString());
        }

            TargetSummary currentTargetSummary = graphSummary.getTargetsSummaryMap().get(currentTarget.getTargetName());
            //Print start of task on current target to target's file and to console
            try {
                taskOutput.outputStartingTaskOnTarget(targetName);
            } catch (FileNotFoundException e) {
                throw new FileNotFound(filePath.getFileName().toString());
            }

            currentTaskThread.start();
            executableTargets.addAll(addNewTargetsToExecutableQueue(currentTarget));

            currentTargetSummary = graphSummary.getTargetsSummaryMap().get(currentTarget.getTargetName());
            //Print target summary to target's file and to console
            try {
                taskOutput.outputEndingTaskOnTarget(new FileOutputStream(filePath.toString(), true),
                        currentTargetSummary);
                taskOutput.outputEndingTaskOnTarget(new PrintStream(System.out),
                        currentTargetSummary);
            } catch (FileNotFoundException e) {
                throw new FileNotFound(filePath.getFileName().toString());
            }

            for(TargetTaskThread taskThread : taskThreadList)
                taskThread.join();
            taskThreadList.clear();
        }

        //Task stopped
        filePath = Paths.get(directoryPath + "\\" +  graph.getGraphName() + " Graph Summary.log");
        try {
            Files.createFile(filePath);
        } catch (IOException e) {
            throw new OpeningFileCrash(filePath.getFileName().toString());
        }

        graphSummary.stopTheClock();
        graphSummary.calculateResults();

        try {
            taskOutput.outputGraphSummary(new FileOutputStream(filePath.toString(), true),
                    graphSummary);
            taskOutput.outputGraphSummary(new PrintStream(System.out),
                    graphSummary);
        } catch (FileNotFoundException e) {
            throw new FileNotFound(filePath.getFileName().toString());
        }
    }
}
