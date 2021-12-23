package myExceptions;

public class FileNotFound extends Exception {

    public FileNotFound(String fileName)
    {
        super("File " + fileName + " not found.");
    }
}
