package patterns;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Patterns
{
    //----------------------------------------Main--------------------------------------//
    public static String LOCAL_HOST = "http://localhost:8080/WebApplication_Web_exploded";
    public static Path WORKING_DIRECTORY_PATH = Paths.get("c:\\gpup-working-dir");
    //----------------------------------------Login--------------------------------------//
    public static String LOGIN = LOCAL_HOST + "/user/login";
    public static String USER_LIST = LOCAL_HOST + "/user/list";
    public static String LOGOUT = LOCAL_HOST + "/user/logout";
    //----------------------------------------Tasks--------------------------------------//
    public static String TASK_LIST = LOCAL_HOST + "/task/list";
    public static String TASK = LOCAL_HOST + "/task";
    public static String TASK_UPDATE = LOCAL_HOST + "/task/update";
    public static String TASK_REGISTER = LOCAL_HOST + "/task/register";
    //----------------------------------------Graphs--------------------------------------//
    public static String GRAPH = LOCAL_HOST + "/graph";
    public static String GRAPH_LIST = LOCAL_HOST + "/graph/list";
}
