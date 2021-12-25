package graphAnalyzers;

import target.Graph;
import target.Target;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PathFinder {
    Set<Target> visitedTargets = new HashSet<>();

    public Boolean prechecksForTargetsConnection(String sourceTargetName, String destTargetName, Graph graph)
    {
        Target sourceTarget ,destinationTarget;

        if(!graph.getGraphTargets().containsKey(sourceTargetName.toLowerCase())
                || !graph.getGraphTargets().containsKey(destTargetName.toLowerCase()))
            return false;

        sourceTarget = graph.getTarget(sourceTargetName);
        destinationTarget = graph.getTarget(destTargetName);

        if (sourceTarget.getTargetPosition().equals(destinationTarget.getTargetPosition()))
        {
            if(sourceTarget.getTargetPosition().equals(Target.TargetPosition.ROOT)
                    || sourceTarget.getTargetPosition().equals(Target.TargetPosition.LEAF))
                return false;
        }
        else if(sourceTarget.getTargetPosition().equals(Target.TargetPosition.INDEPENDENT)
                || destinationTarget.getTargetPosition().equals(Target.TargetPosition.INDEPENDENT))
            return false;

        return true;
    }
    public ArrayList<String> getPathsFromTargets(Target source, Target dest, Target.Connection connection)
    {
        ArrayList<String> currentPath = new ArrayList<>();
        ArrayList<String> returnedPaths;
        Set<Target> nextTargetsOnCurrentPath;

        if(source.equals(dest))
        {
            currentPath.add(source.getTargetName());
            return currentPath;
        }
        else if(visitedTargets.contains(source))
            return currentPath;
        else if (connection.equals(Target.Connection.DEPENDS_ON))
            nextTargetsOnCurrentPath = source.getDependsOnTargets();
        else // (connection.equals(Target.Connection.REQUIRED_FOR))
            nextTargetsOnCurrentPath = source.getRequiredForTargets();

        if(nextTargetsOnCurrentPath.size() == 0)
            return currentPath;

        for(Target currentTarget : nextTargetsOnCurrentPath)
        {
            returnedPaths = getPathsFromTargets(currentTarget, dest, connection);

            for(String path : returnedPaths)
                currentPath.add(source.getTargetName() + " -> " + path);
        }

        return currentPath;
    }


}
