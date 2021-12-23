package myExceptions;

public class DoubledTarget extends Exception {

    public DoubledTarget(String targetName)
    {
        super("Invalid file: the target " + targetName + " appear at least 2 times in the file.");
    }
}