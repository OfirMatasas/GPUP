package target;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Target implements Serializable {
    //--------------------------------------------------Enums-------------------------------------------------------//
    public enum TargetProperty { LEAF, MIDDLE, ROOT, INDEPENDENT }
    public enum Connection { REQUIRED_FOR, DEPENDS_ON }

    //--------------------------------------------------Members-----------------------------------------------------//
    private final Set<Target> dependsOnTargets;
    private final Set<Target> requiredForTargets;
    private TargetProperty targetProperty;
    private String targetName;
    private String extraInformation;

    //------------------------------------------------Constructors--------------------------------------------------//
    public Target() {
        this.dependsOnTargets = new HashSet<>();
        this.requiredForTargets = new HashSet<>();
        this.targetProperty = TargetProperty.INDEPENDENT;
    }

    //--------------------------------------------------Getters-----------------------------------------------------//
    public String getTargetName() {
        return targetName;
    }

    public String getExtraInformation() {
        return extraInformation;
    }

    public Set<Target> getDependsOnTargets() {
        return dependsOnTargets;
    }

    public Set<Target> getRequiredForTargets() {
        return requiredForTargets;
    }

    public TargetProperty getTargetProperty() {
        return targetProperty;
    }

    //--------------------------------------------------Setters-----------------------------------------------------//
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void setExtraInformation(String extraInformation) {
        this.extraInformation = extraInformation;
    }

    public void setTargetProperty(TargetProperty targetProperty) {
        this.targetProperty = targetProperty;
    }

    //--------------------------------------------------Methods-----------------------------------------------------//
    public void addToDependsOn(Target dependsOn) { dependsOnTargets.add(dependsOn); }

    public void addToRequiredFor(Target requiredFor) { requiredForTargets.add(requiredFor); }

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