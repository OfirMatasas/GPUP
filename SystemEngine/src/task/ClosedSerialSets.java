package task;

import target.Target;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClosedSerialSets {
    private final Set<String> closedSerialSets;

    public ClosedSerialSets() {
        this.closedSerialSets = new HashSet<>();
    }

    public synchronized void addClosedSerialSets(Target target)
    {
        closedSerialSets.addAll(target.getSerialSets());
    }

    public synchronized void removeClosedSerialSets(Target target)
    {
        closedSerialSets.removeAll(target.getSerialSets());
    }

    public synchronized Boolean checkForCommonItems(Set<String> otherSerialSet)
    {
        return !Collections.disjoint(closedSerialSets, otherSerialSet);
    }
}
