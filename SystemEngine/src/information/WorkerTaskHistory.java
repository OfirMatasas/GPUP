package information;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorkerTaskHistory {
    private final Map<String, String> targetsLog;
    private final Integer pricingPerTarget;
    private Integer executedTargets;

    public WorkerTaskHistory(Integer pricingPerTarget) {
        this.targetsLog = new HashMap<>();
        this.pricingPerTarget = pricingPerTarget;
        this.executedTargets = 0;
    }

    public synchronized Set<String> getWorkingOnTargets() {
        return this.targetsLog.keySet();
    }

    public synchronized Integer getPricingPerTarget() {
        return this.pricingPerTarget;
    }

    public synchronized void newWorkedOnTarget(String targetName) { this.targetsLog.put(targetName, ""); }

    public synchronized void newExecutedTarget() { ++this.executedTargets; }

    public synchronized Integer getTotalCreditsFromTask() { return this.pricingPerTarget * this.executedTargets; }

    public synchronized Integer getExecutedTargets() { return this.executedTargets; }
}