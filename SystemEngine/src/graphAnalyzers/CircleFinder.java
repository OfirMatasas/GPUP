package graphAnalyzers;

import target.Target;
import java.util.HashSet;
import java.util.Set;

public class CircleFinder {
    private Boolean circled;
    private String circlePath;
    Set<Target> visited;

    public Boolean getCircled() {
        return circled;
    }

    public String getCirclePath() {
        return circlePath;
    }

    public void checkIfCircled(Target target)
    {
        circlePath = null;
        circled = false;
        visited = new HashSet<>();

        for(Target current : target.getDependsOnTargets())
        {
            checkIfCircledRec(target, current);

            if(circled)
            {
                circlePath = current.getTargetName() + " -> " + circlePath;
                circlePath = target.getTargetName() + " -> " + circlePath;
                break;
            }
        }
    }

    private void checkIfCircledRec(Target origin, Target current)
    {
        if(current.equals(origin))
        {
            circled = true;
            circlePath = current.getTargetName();
            return;
        }

        if(visited.contains(current))
            return;
        else
            visited.add(current);

        for(Target nextTarget : current.getDependsOnTargets())
        {
            checkIfCircledRec(origin, nextTarget);
            if(circled)
            {
                if(!nextTarget.equals(origin))
                    circlePath =  nextTarget.getTargetName() +  " -> " + circlePath;

                break;
            }
        }
    }
}