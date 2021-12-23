package myExceptions;

public class InvalidThreadsNumber extends Exception{

    public InvalidThreadsNumber()
    {
        super("Invalid number of threads");
    }
}