package myExceptions;

public class NotUniqueSerialName extends Exception{

    public NotUniqueSerialName(String serialName)
    {
        super("There are at least 2 serial sets named " + serialName);
    }
}