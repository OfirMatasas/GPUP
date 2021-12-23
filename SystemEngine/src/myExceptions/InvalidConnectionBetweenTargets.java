package myExceptions;

public class InvalidConnectionBetweenTargets extends Exception{

    public InvalidConnectionBetweenTargets(String target1, String target2)
    {
        super("Invalid file: invalid connection between target "
                + target1 + " and target " + target2 + ".");
    }
}