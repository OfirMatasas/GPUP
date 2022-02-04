package myExceptions;

public class TaskPricingNotFound extends Exception {

    public TaskPricingNotFound()
    {
        super("There's no pricing for tasks on graph!");
    }
}