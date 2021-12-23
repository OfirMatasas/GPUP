package target;

import java.io.Serializable;
import java.util.*;

public class Graph implements Serializable {
    //--------------------------------------------------Members-----------------------------------------------------//
    private String graphName;
    private Map<String, Target> graphTargets;
    private Map<Target.TargetProperty, Set<Target>> targetsByProperties;
    private Map<String, Set<String>> serialSetsMap;
    private Set<String> serialSetsNames;

    //------------------------------------------------Constructors--------------------------------------------------//
    public Graph() {
        this.graphTargets = new HashMap<>();
        this.targetsByProperties = new HashMap<>();
        this.targetsByProperties.put(Target.TargetProperty.LEAF, new HashSet<>());
        this.targetsByProperties.put(Target.TargetProperty.INDEPENDENT, new HashSet<>());
        this.targetsByProperties.put(Target.TargetProperty.ROOT, new HashSet<>());
        this.targetsByProperties.put(Target.TargetProperty.MIDDLE, new HashSet<>());
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
    public int numberOfTargetsByProperty(Target.TargetProperty property)
    {
        return targetsByProperties.get(property).size();
    }

    public Boolean isEmpty()
    {
        return graphTargets.size() == 0;
    }

    public void addNewTargetToTheGraph(Target... newTargets) {
        for(Target currentTarget : newTargets)
            graphTargets.put(currentTarget.getTargetName().toLowerCase(), currentTarget);

        calculateProperties();
    }

    public void calculateProperties()
    {
        clearTargetsByProperties();
        Integer valCount = 0;
        Target.TargetProperty currentTargetProperty;

        for(Target currentTarget : graphTargets.values())
        {
            if(currentTarget.getDependsOnTargets().size() == 0
                    && currentTarget.getRequiredForTargets().size() == 0)
            {//Independent
                currentTargetProperty = Target.TargetProperty.INDEPENDENT;
            }
            else if(currentTarget.getDependsOnTargets().size() == 0)
            {//Leaf
                currentTargetProperty = Target.TargetProperty.LEAF;
            }
            else if(currentTarget.getRequiredForTargets().size() == 0)
            {//Root
                currentTargetProperty = Target.TargetProperty.ROOT  ;
            }
            else
            {//Middle
                currentTargetProperty = Target.TargetProperty.MIDDLE;
            }
            targetsByProperties.get(currentTargetProperty).add(currentTarget);
            currentTarget.setTargetProperty(currentTargetProperty);
        }
    }

    private void clearTargetsByProperties()
    {
        targetsByProperties.get(Target.TargetProperty.ROOT).clear();
        targetsByProperties.get(Target.TargetProperty.MIDDLE).clear();
        targetsByProperties.get(Target.TargetProperty.LEAF).clear();
        targetsByProperties.get(Target.TargetProperty.INDEPENDENT).clear();
    }
}