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
    private TaskParameters taskParameters;
    private Graph graph;
    private GraphSummary graphSummary;
    private TaskOutput taskOutput;

    public SimulationThread(TaskParameters taskParameters, Graph graph, GraphSummary graphSummary) {
        this.taskParameters = taskParameters;
        this.graph = graph;
        this.graphSummary = graphSummary;

        ArrayList<OutputStream> outputStreams = new ArrayList<>();
        outputStreams.add(new FileOutputStream(filePath.toString()),
                currentTargetSummary)
        this.taskOutput = new TaskOutput();
    }

    @Override
    public void run() {
        Path directoryPath = null;
        try {
            directoryPath = taskOutput.createNewDirectoryOfTaskLogs("Simulation Task", Paths.get(graphSummary.getWorkingDirectory()));
        } catch (OpeningFileCrash e) {
            e.printStackTrace();
        }
        Path filePath;

        //Check if there are any task parameters saved from last execution
        if(targetsParameters == null || !requirements.reuseTaskParameters())
        {
            TaskParameters taskParameters = requirements.getTaskParametersFromUser();

            //Update target parameters
            updateTaskParameters(taskParameters);
        }
        else
            graphSummary.changePredictedTime(graph, targetsParameters);

        //Make a set of executable targets
        ArrayBlockingQueue<Target> executableTargets = makeExecutableTargetsQueue(fromScratch);
        Target currentTarget;
        TargetTaskThread currentTaskThread;
        ArrayList<TargetTaskThread> taskThreadList = new ArrayList<>();

        //Starting task on graph
        taskOutput.printStartOfTaskOnGraph(graph.getGraphName());
        graphSummary.startTheClock();

        while(!executableTargets.isEmpty())
        {
            currentTarget = executableTargets.poll();
            currentTaskThread = new TargetTaskThread(graphSummary, currentTarget, targetsParameters.get(currentTarget));
            taskThreadList.add(currentTaskThread);

            //Creating a file for each target
            filePath = Paths.get(directoryPath + "\\" + currentTarget.getTargetName() + ".log");
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw new OpeningFileCrash(filePath.getFileName().toString());
            }

            TargetSummary currentTargetSummary = graphSummary.getTargetsSummaryMap().get(currentTarget.getTargetName());
            //Print start of task on current target to target's file and to console
            try {
                taskOutput.outputStartingTaskOnTarget(new FileOutputStream(filePath.toString()),
                        currentTargetSummary);
                taskOutput.outputStartingTaskOnTarget(new PrintStream(System.out),
                        currentTargetSummary);
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
