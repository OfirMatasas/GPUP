package tableItems;

public class WorkerChosenTaskInformationTableItem {
    private final String name;
    private final String status;
    private final Integer workers;
    private final Integer finishedTargets;
    private final Integer earnedCredits;

    public WorkerChosenTaskInformationTableItem(String name, String status, Integer workers, Integer finishedTargets, Integer earnedCredits) {
        this.name = name;
        this.status = status;
        this.workers = workers;
        this.finishedTargets = finishedTargets;
        this.earnedCredits = earnedCredits;
    }

    public String getName() {
        return this.name;
    }

    public String getStatus() {
        return this.status;
    }

    public Integer getWorkers() {
        return this.workers;
    }

    public Integer getFinishedTargets() {
        return this.finishedTargets;
    }

    public Integer getEarnedCredits() {
        return this.earnedCredits;
    }
}
