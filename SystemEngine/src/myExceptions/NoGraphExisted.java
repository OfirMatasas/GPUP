package myExceptions;

public class NoGraphExisted extends Exception {

    public NoGraphExisted()
    {
        super("Please load a graph first!");
    }
}