package users;

import java.util.HashSet;
import java.util.Set;

public class UsersLists {
    private final Set<String> adminsList;
    private final Set<String> workersList;

    public UsersLists() {
        this.adminsList = new HashSet<>();
        this.workersList = new HashSet<>();
    }

    public Set<String> getAdminsList() {
        return this.adminsList;
    }

    public Set<String> getWorkersList() {
        return this.workersList;
    }
}
