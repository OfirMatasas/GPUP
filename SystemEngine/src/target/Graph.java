package target;

import java.io.Serializable;
import java.util.*;

public class Graph implements Serializable {
    //--------------------------------------------------Members-----------------------------------------------------//
    public static enum TaskType { Simulation, Compilation }

    private String graphName;
    private final Map<String, Target> graphTargets;
    private final Map<Target.TargetPosition, Set<Target>> targetsByPositions;
    private Map<String, Set<String>> serialSetsMap;
    private Set<String> serialSetsNames;
    private int MaxParallelism;
    private String uploader;
    private Map<TaskType, Integer> tasksPricesMap;
    //------------------------------------------------Constructors--------------------------------------------------//
    public Graph() {
        this.graphTargets = new HashMap<>();
        this.targetsByPositions = new HashMap<>();
        this.targetsByPositions.put(Target.TargetPosition.LEAF, new HashSet<>());
        this.targetsByPositions.put(Target.TargetPosition.INDEPENDENT, new HashSet<>());
        this.targetsByPositions.put(Target.TargetPosition.ROOT, new HashSet<>());
        this.targetsByPositions.put(Target.TargetPosition.MIDDLE, new HashSet<>());
        this.serialSetsNames = new HashSet<>();
        this.serialSetsMap = new HashMap<>();
        this.tasksPricesMap = new HashMap<>();
    }

    //--------------------------------------------------Getters-----------------------------------------------------//
    public String getGraphName() {
        return this.graphName;
    }

    public Map<String, Target> getGraphTargets() {
        return this.graphTargets;
    }

    public Target getTarget(String targetName)
    {
        return this.graphTargets.get(targetName.toLowerCase());
    }

    public Set<String> getSerialSetsNames() {
        return this.serialSetsNames;
    }

    public Map<String, Set<String>> getSerialSetsMap() {
        return this.serialSetsMap;
    }
    public int getMaxParallelism() {return this.MaxParallelism;}

    public String getUploader() {
        return this.uploader;
    }

    public Map<TaskType, Integer> getTasksPricesMap() {
        return this.tasksPricesMap;
    }

    //--------------------------------------------------Setters-----------------------------------------------------//
    public void setGraphName(String graphName) {
        this.graphName = graphName;
    }

    public void setSerialSetsMap(Map<String, Set<String>> serialSetsMap) {
        this.serialSetsMap = serialSetsMap;
    }

    public void setSerialSetsNames(Set<String> serialSetsNames) {
        this.serialSetsNames = serialSetsNames;
    }

    public void setMaxParallelism(int maxParallelism) {
        this.MaxParallelism = maxParallelism;}

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public void addTaskAndPrice(TaskType taskType, Integer taskPrice)
    {
        if(this.tasksPricesMap.containsKey(taskType))
            return;

        this.tasksPricesMap.put(taskType, taskPrice);
    }

    //--------------------------------------------------Methods-----------------------------------------------------//
    public int numberOfTargetsByProperty(Target.TargetPosition position)
    {
        return this.targetsByPositions.get(position).size();
    }

    public Boolean isEmpty()
    {
        return this.graphTargets.size() == 0;
    }

    public void addNewTargetToTheGraph(Target... newTargets) {
        for(Target currentTarget : newTargets)
            this.graphTargets.put(currentTarget.getTargetName().toLowerCase(), currentTarget);

        calculatePositions();
    }

    public void calculatePositions()
    {
        clearTargetsByProperties();
        Integer valCount = 0;
        Target.TargetPosition currentTargetProperty;

        for(Target currentTarget : this.graphTargets.values())
        {
            if(currentTarget.getDependsOnTargets().size() == 0
                    && currentTarget.getRequiredForTargets().size() == 0)
            {//Independent
                currentTargetProperty = Target.TargetPosition.INDEPENDENT;
            }
            else if(currentTarget.getDependsOnTargets().size() == 0)
            {//Leaf
                currentTargetProperty = Target.TargetPosition.LEAF;
            }
            else if(currentTarget.getRequiredForTargets().size() == 0)
            {//Root
                currentTargetProperty = Target.TargetPosition.ROOT  ;
            }
            else
            {//Middle
                currentTargetProperty = Target.TargetPosition.MIDDLE;
            }
            this.targetsByPositions.get(currentTargetProperty).add(currentTarget);
            currentTarget.setTargetPosition(currentTargetProperty);
        }
    }

    public void calculateAllDependsOn()
    {
        for(Target currentTarget : this.graphTargets.values())
            currentTarget.calculateAllDependsOnTargets(currentTarget);
    }

    public void calculateAllRequiredFor()
    {
        for(Target currentTarget : this.graphTargets.values())
            currentTarget.calculateAllRequiredForTargets(currentTarget);
    }

    private void clearTargetsByProperties()
    {
        this.targetsByPositions.get(Target.TargetPosition.ROOT).clear();
        this.targetsByPositions.get(Target.TargetPosition.MIDDLE).clear();
        this.targetsByPositions.get(Target.TargetPosition.LEAF).clear();
        this.targetsByPositions.get(Target.TargetPosition.INDEPENDENT).clear();
    }
}