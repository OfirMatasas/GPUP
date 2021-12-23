package myExceptions;

public class NotDirectory extends Exception {

    public NotDirectory(String workingDirectoryPath) {
        super("Invalid working directory:\n" + workingDirectoryPath + " is not a directory ");
    }
}