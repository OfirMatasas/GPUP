package target;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Target implements Serializable
{
    //--------------------------------------------------Enums-------------------------------------------------------//
    public enum TargetPosition { LEAF, MIDDLE, ROOT, INDEPENDENT }
    public enum Connection { REQUIRED_FOR, DEPENDS_ON }
    //--------------------------------------------------Members-----------------------------------------------------//
    private final Set<Target> dependsOnTargets;
    private final Set<Target> requiredForTargets;
    private final Set<String> serialSets;
    private TargetPosition targetPosition;
    private String targetName;
    private String extraInformation;
    private final Set<String> allDependsOnTargets;
    private final Set<String> allRequiredForTargets;

    //------------------------------------------------Constructors--------------------------------------------------//
    public Target() {
        this.dependsOnTargets = new HashSet<>();
        this.requiredForTargets = new HashSet<>();
        this.targetPosition = TargetPosition.INDEPENDENT;
        this.serialSets = new HashSet<>();
        this.allDependsOnTargets = new HashSet<>();
        this.allRequiredForTargets = new HashSet<>();
    }

    //--------------------------------------------------Getters-----------------------------------------------------//
    public String getTargetName() {
        return targetName;
    }

    public Set<Target> getDependsOnTargets() {
        return dependsOnTargets;
    }

    public Set<String> getSerialSets() {
        return serialSets;
    }

    public Set<Target> getRequiredForTargets() {
        return requiredForTargets;
    }

    public TargetPosition getTargetPosition() {
        return targetPosition;
    }

    public Set<String> getAllDependsOnTargets() {
        return allDependsOnTargets;
    }

    public Set<String> getAllRequiredForTargets() {
        return allRequiredForTargets;
    }
    public String getExtraInformation() {return extraInformation;}

    //--------------------------------------------------Setters-----------------------------------------------------//
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void setExtraInformation(String extraInformation) {
        this.extraInformation = extraInformation;
    }

    public void setTargetPosition(TargetPosition targetPosition) {
        this.targetPosition = targetPosition;
    }

    public void addSerialSet(String newSerialSet) { serialSets.add(newSerialSet); }

    //--------------------------------------------------Methods-----------------------------------------------------//
    public void addToDependsOn(Target dependsOn) { dependsOnTargets.add(dependsOn); }

    public void addToRequiredFor(Target requiredFor) { requiredForTargets.add(requiredFor); }

    public void calculateAllDependsOnTargets(Target target)
    {
        for(Target currentTarget : target.getDependsOnTargets())
        {
            if(allDependsOnTargets.contains(currentTarget.getTargetName()))
                continue;

            allDependsOnTargets.add(currentTarget.getTargetName());
            calculateAllDependsOnTargets(currentTarget);
        }
    }

    public void calculateAllRequiredForTargets(Target target)
    {
        for(Target currentTarget : target.getRequiredForTargets())
        {
            if(allRequiredForTargets.contains(currentTarget.getTargetName()))
                continue;

            allRequiredForTargets.add(currentTarget.getTargetName());
            calculateAllRequiredForTargets(currentTarget);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        return Objects.equals(targetName, target.targetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetName);
    }
}