package myExceptions;

public class DoubledTargetInSerialSet extends Exception {

    public DoubledTargetInSerialSet(String targetName, String serialSetName)
    {
        super("The target " + targetName + " appear at least 2 times in serial set " + serialSetName + ".");
    }
}