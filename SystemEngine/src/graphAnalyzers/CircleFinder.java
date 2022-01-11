package graphAnalyzers;

import target.Target;
import java.util.HashSet;
import java.util.Set;

public class CircleFinder {
    private Boolean circled;
    private String circlePath;
    private Set<Target> visited;

    public Boolean getCircled() {
        return this.circled;
    }

    public String getCirclePath() {
        return this.circlePath;
    }

    public void checkIfCircled(Target target)
    {
        this.circlePath = null;
        this.circled = false;
        this.visited = new HashSet<>();

        for(Target current : target.getDependsOnTargets())
        {
            checkIfCircledRec(target, current);

            if(this.circled)
            {
                this.circlePath = current.getTargetName() + " ↓ " + this.circlePath;
                this.circlePath = target.getTargetName() + " ↓ " + this.circlePath;
                break;
            }
        }
    }

    private void checkIfCircledRec(Target origin, Target current)
    {
        if(current.equals(origin))
        {
            this.circled = true;
            this.circlePath = current.getTargetName();
            return;
        }

        if(this.visited.contains(current))
            return;
        else
            this.visited.add(current);

        for(Target nextTarget : current.getDependsOnTargets())
        {
            checkIfCircledRec(origin, nextTarget);
            if(this.circled)
            {
                if(!nextTarget.equals(origin))
                    this.circlePath =  nextTarget.getTargetName() +  " ↓ " + this.circlePath;

                break;
            }
        }
    }
}