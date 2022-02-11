package information;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorkerTaskHistory {
    private final Map<String, String> targetsLog;
    private final Integer pricingPerTarget;

    public WorkerTaskHistory(Integer pricingPerTarget) {
        this.targetsLog = new HashMap<>();
        this.pricingPerTarget = pricingPerTarget;
    }

    public synchronized Set<String> getTargets() {
        return this.targetsLog.keySet();
    }

    public synchronized Integer getPricingPerTarget() {
        return this.pricingPerTarget;
    }

    public synchronized void newWorkedOnTarget(String targetName) { this.targetsLog.put(targetName, ""); }

    public synchronized Integer getTotalCreditsFromTask() { return this.pricingPerTarget * this.targetsLog.size(); }
}