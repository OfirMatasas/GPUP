package dtos;

import tableItems.WorkerChosenTaskInformationTableItem;

public class WorkerChosenTaskDTO {
    private WorkerChosenTaskInformationTableItem item;
    private final Integer totalTargets;
    private final Integer finishedTargets;

    public WorkerChosenTaskDTO(WorkerChosenTaskInformationTableItem item, Integer totalTargets, Integer finishedTargets) {
        this.item = item;
        this.totalTargets = totalTargets;
        this.finishedTargets = finishedTargets;
    }

    public WorkerChosenTaskInformationTableItem getItem() {
        return this.item;
    }

    public Integer getTotalTargets() {
        return this.totalTargets;
    }

    public Integer getFinishedTargets() {
        return this.finishedTargets;
    }
}
