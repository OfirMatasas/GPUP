package dtos;

import tableItems.WorkerChosenTargetInformationTableItem;

public class WorkerChosenTargetDTO {
    private WorkerChosenTargetInformationTableItem tableItem;
    private String logHistory = "";

    public WorkerChosenTargetDTO(WorkerChosenTargetInformationTableItem tableItem, String logHistory) {
        this.tableItem = tableItem;
        this.logHistory = logHistory;
    }

    public WorkerChosenTargetInformationTableItem getTableItem() {
        return this.tableItem;
    }

    public String getLogHistory() {
        return this.logHistory;
    }

    public void updateLogHistory(String newInfo) {
        this.logHistory += newInfo + "\n";
    }
}
