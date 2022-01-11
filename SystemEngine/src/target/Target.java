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
    private String FQN;

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
        return this.targetName;
    }

    public Set<Target> getDependsOnTargets() {
        return this.dependsOnTargets;
    }

    public Set<String> getSerialSets() {
        return this.serialSets;
    }

    public Set<Target> getRequiredForTargets() {
        return this.requiredForTargets;
    }

    public TargetPosition getTargetPosition() {
        return this.targetPosition;
    }

    public Set<String> getAllDependsOnTargets() {
        return this.allDependsOnTargets;
    }

    public Set<String> getAllRequiredForTargets() {
        return this.allRequiredForTargets;
    }

    public String getExtraInformation() {return this.extraInformation;}

    public String getFQN() {
        return this.FQN;
    }

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

    public void addSerialSet(String newSerialSet) {
        this.serialSets.add(newSerialSet); }

    public void setFQN(String FQN) {
        this.FQN = FQN;
    }

    //--------------------------------------------------Methods-----------------------------------------------------//
    public void addToDependsOn(Target dependsOn) {
        this.dependsOnTargets.add(dependsOn); }

    public void addToRequiredFor(Target requiredFor) {
        this.requiredForTargets.add(requiredFor); }

    public void calculateAllDependsOnTargets(Target target)
    {
        for(Target currentTarget : target.getDependsOnTargets())
        {
            if(this.allDependsOnTargets.contains(currentTarget.getTargetName()))
                continue;

            this.allDependsOnTargets.add(currentTarget.getTargetName());
            calculateAllDependsOnTargets(currentTarget);
        }
    }

    public void calculateAllRequiredForTargets(Target target)
    {
        for(Target currentTarget : target.getRequiredForTargets())
        {
            if(this.allRequiredForTargets.contains(currentTarget.getTargetName()))
                continue;

            this.allRequiredForTargets.add(currentTarget.getTargetName());
            calculateAllRequiredForTargets(currentTarget);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        return Objects.equals(this.targetName, target.targetName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.targetName);
    }
}