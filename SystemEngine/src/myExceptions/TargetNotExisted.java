package myExceptions;

public class TargetNotExisted extends Exception{

    public TargetNotExisted(String targetName, String serialSetName)
    {
        super("The target " + targetName + " doesn't exist in the graph, but exist in " + serialSetName + " serial set.");
    }
}