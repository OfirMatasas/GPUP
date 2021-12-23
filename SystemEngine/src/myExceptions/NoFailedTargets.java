package myExceptions;

public class NoFailedTargets extends Exception {

    public NoFailedTargets()
    {
        super("All the targets in the graph already succeeded!");
    }
}
