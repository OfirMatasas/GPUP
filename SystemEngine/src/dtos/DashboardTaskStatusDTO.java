package dtos;

public class DashboardTaskStatusDTO {

    private final String taskStatus;
    private final Integer totalWorkers;
    private final Integer totalPayment;

    public DashboardTaskStatusDTO(String taskStatus, Integer totalWorkers, Integer totalPayment) {
        this.taskStatus = taskStatus;
        this.totalWorkers = totalWorkers;
        this.totalPayment = totalPayment;
    }

    public String getTaskStatus() {
        return this.taskStatus;
    }

    public Integer getTotalWorkers() {
        return this.totalWorkers;
    }

    public Integer getTotalPayment() {
        return this.totalPayment;
    }
}
