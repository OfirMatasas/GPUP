package information;

import java.util.Set;

public class WorkerTaskStatusTableItem {
    private final String type;
    private final String status;
    private final Integer workers;
    private final Integer totalPayment;
    private final String registered;

    public WorkerTaskStatusTableItem(String type, String status, Set<String> registeredWorkers, Integer totalPayment, String userName) {
        this.type = type;
        this.status = status;
        this.workers = registeredWorkers.size();
        this.totalPayment = totalPayment;
        this.registered = registeredWorkers.contains(userName) ? "Yes" : "No";
    }

    public String getType() {
        return this.type;
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

    public String getRegistered() {
        return this.registered;
    }
}
