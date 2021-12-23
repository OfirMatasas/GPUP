package myExceptions;

public class OpeningFileCrash extends Exception{

    public OpeningFileCrash(String fileName)
    {
        super("Couldn't open file.");
    }
}
