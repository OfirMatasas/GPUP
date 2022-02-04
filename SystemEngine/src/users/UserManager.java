package users;

import java.util.Collections;
import java.util.Set;


public class UserManager {

    private final UsersLists usersLists;

    public UserManager() {
        this.usersLists = new UsersLists(); }

    public synchronized void addAdmin(String username) {
        this.usersLists.getAdminsList().add(username);
    }
    public synchronized Set<String> getAdmins() {
        return Collections.unmodifiableSet(this.usersLists.getAdminsList());
    }

    public synchronized Set<String> getWorkers() {
        return Collections.unmodifiableSet(this.usersLists.getWorkersList());
    }

    public synchronized void addWorker(String username) {
        this.usersLists.getWorkersList().add(username);
    }

    public synchronized void removeUser(String username) {
        if(this.usersLists.getWorkersList().contains(username))
            this.usersLists.getWorkersList().remove(username);
        else
            this.usersLists.getAdminsList().remove(username);
    }

    public boolean isUserExists(String username) {
        return (this.usersLists.getAdminsList().contains(username) || this.usersLists.getWorkersList().contains(username));
    }

    public UsersLists getUsersLists() { return this.usersLists; }
}
