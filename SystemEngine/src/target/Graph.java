package target;

import java.io.Serializable;
import java.util.*;

public class Graph implements Serializable {
    //--------------------------------------------------Members-----------------------------------------------------//
    private String graphName;
    private final Map<String, Target> graphTargets;
    private final Map<Target.TargetPosition, Set<Target>> targetsByPositions;
    private Map<String, Set<String>> serialSetsMap;
    private Set<String> serialSetsNames;

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
    }

    //--------------------------------------------------Getters-----------------------------------------------------//
    public String getGraphName() {
        return graphName;
    }

    public Map<String, Target> getGraphTargets() {
        return graphTargets;
    }

    public Target getTarget(String targetName)
    {
        return graphTargets.get(targetName.toLowerCase());
    }

    public Set<String> getSerialSetsNames() {
        return serialSetsNames;
    }

    public Map<String, Set<String>> getSerialSetsMap() {
        return serialSetsMap;
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

    //--------------------------------------------------Methods-----------------------------------------------------//
    public int numberOfTargetsByProperty(Target.TargetPosition position)
    {
        return targetsByPositions.get(position).size();
    }

    public Boolean isEmpty()
    {
        return graphTargets.size() == 0;
    }

    public void addNewTargetToTheGraph(Target... newTargets) {
        for(Target currentTarget : newTargets)
            graphTargets.put(currentTarget.getTargetName().toLowerCase(), currentTarget);

        calculatePositions();
    }

    public void calculatePositions()
    {
        clearTargetsByProperties();
        Integer valCount = 0;
        Target.TargetPosition currentTargetProperty;

        for(Target currentTarget : graphTargets.values())
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
            targetsByPositions.get(currentTargetProperty).add(currentTarget);
            currentTarget.setTargetPosition(currentTargetProperty);
        }
    }

    public void calculateAllDependsOn()
    {
        for(Target currentTarget : graphTargets.values())
            currentTarget.calculateAllDependsOnTargets(currentTarget);
    }

    public void calculateAllRequiredFor()
    {
        for(Target currentTarget : graphTargets.values())
            currentTarget.calculateAllRequiredForTargets(currentTarget);
    }

    private void clearTargetsByProperties()
    {
        targetsByPositions.get(Target.TargetPosition.ROOT).clear();
        targetsByPositions.get(Target.TargetPosition.MIDDLE).clear();
        targetsByPositions.get(Target.TargetPosition.LEAF).clear();
        targetsByPositions.get(Target.TargetPosition.INDEPENDENT).clear();
    }
}