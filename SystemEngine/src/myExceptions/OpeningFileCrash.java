package myExceptions;

import java.nio.file.Path;

public class OpeningFileCrash extends Exception{

    public OpeningFileCrash(Path fileName)
    {
        super("Couldn't open the file " + fileName.getFileName() + ".");
    }
}
