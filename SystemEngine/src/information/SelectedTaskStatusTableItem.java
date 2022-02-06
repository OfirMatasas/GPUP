package information;

public class SelectedTaskStatusTableItem {
    private final String status;
    private final Integer workers;
    private final Integer totalPayment;

    public SelectedTaskStatusTableItem(String status, Integer workers, Integer totalPayment) {
        this.status = status;
        this.workers = workers;
        this.totalPayment = totalPayment;
    }

    public String getStatus() {
        return this.status;
    }

    public Integer getWorkers() {
        return this.workers;
    }

    public Integer getTotalPayment() {
        return this.totalPayment;
    }
}
