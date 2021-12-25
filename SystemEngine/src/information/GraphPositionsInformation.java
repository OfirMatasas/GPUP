package information;

public class GraphPositionsInformation {
    private final int roots;
    private final int middles;
    private final int leaves;
    private final int independents;

    public GraphPositionsInformation(int roots, int middles, int leaves, int independents) {
        this.roots = roots;
        this.middles = middles;
        this.leaves = leaves;
        this.independents = independents;
    }

    public int getRoots() {
        return roots;
    }

    public int getMiddles() {
        return middles;
    }

    public int getLeaves() {
        return leaves;
    }

    public int getIndependents() {
        return independents;
    }
}
