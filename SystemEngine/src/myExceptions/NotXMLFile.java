package myExceptions;

public class NotXMLFile extends Exception{

    public NotXMLFile(String fileName) {
        super("Invalid file:" + fileName + " is not xml type.");
    }
}