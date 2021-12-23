package myExceptions;

public class EmptyGraph extends Exception {

    public EmptyGraph()
    {
        super("The graph is empty!");
    }
}
