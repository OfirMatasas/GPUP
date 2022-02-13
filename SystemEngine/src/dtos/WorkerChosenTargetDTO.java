package dtos;

import tableItems.WorkerChosenTargetInformationTableItem;

public class WorkerChosenTargetDTO {
    private final WorkerChosenTargetInformationTableItem item;
    private final String log;

    public WorkerChosenTargetDTO(WorkerChosenTargetInformationTableItem item, String log) {
        this.item = item;
        this.log = log;
    }

    public WorkerChosenTargetInformationTableItem getItem() {
        return this.item;
    }

    public String getLog() {
        return this.log;
    }
}