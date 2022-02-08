package managers;

import users.UsersLists;

import java.util.*;


public class UserManager {

    private final UsersLists usersLists;
    private final Set<String> adminsNames = new HashSet<>();
    private final Set<String> workersNames = new HashSet<>();

    public UserManager() {
        this.usersLists = new UsersLists(); }

    public synchronized void addAdmin(String adminName) {

        this.usersLists.getAdminsList().add(adminName);
        this.adminsNames.add(adminName);
    }

    public synchronized Set<String> getAdmins() {
        return Collections.unmodifiableSet(this.usersLists.getAdminsList());
    }

    public synchronized Set<String> getWorkers() {
        return Collections.unmodifiableSet(this.usersLists.getWorkersList());
    }

    public synchronized void addWorker(String workerName) {
        this.usersLists.getWorkersList().add(workerName);
        this.workersNames.add(workerName);
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

    public boolean isValidLogin(String userName, boolean isAdmin)
    {
        return (isAdmin ? !this.workersNames.contains(userName) : !this.adminsNames.contains(userName));

//        if(isAdmin) //Trying to log in as admin
//            return !this.workersNames.contains(userName);
//        else //Trying to log in as worker
//            return !this.adminsNames.contains(userName);
    }

    public UsersLists getUsersLists() { return this.usersLists; }
}
