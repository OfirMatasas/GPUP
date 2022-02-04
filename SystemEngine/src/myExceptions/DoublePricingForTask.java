package myExceptions;

public class DoublePricingForTask extends Exception {

    public DoublePricingForTask(String taskName)
    {
        super("There are at least 2 pricing tags for " + taskName + " on current graph!");
    }
}