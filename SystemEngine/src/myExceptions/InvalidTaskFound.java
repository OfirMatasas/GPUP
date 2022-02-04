package myExceptions;

public class InvalidTaskFound extends Exception {

    public InvalidTaskFound(String taskName)
    {
        super("The task " + taskName + " is not supported!");
    }
}