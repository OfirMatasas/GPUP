package information;

import java.util.HashSet;
import java.util.Set;

public class WorkerWorkOnTask {
    private final Set<String> targetsWorkedOn;
    private final Integer pricingPerTarget;

    public WorkerWorkOnTask(Integer pricingPerTarget) {
        this.targetsWorkedOn = new HashSet<>();
        this.pricingPerTarget = pricingPerTarget;
    }

    public Set<String> getTargets() {
        return this.targetsWorkedOn;
    }

    public Integer getPricingPerTarget() {
        return this.pricingPerTarget;
    }

    public void newWorkedOnTarget(String targetName) { this.targetsWorkedOn.add(targetName); }
}
