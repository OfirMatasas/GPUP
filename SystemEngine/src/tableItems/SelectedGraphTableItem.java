package tableItems;

public class SelectedGraphTableItem {
    private Integer targets;
    private Integer roots;
    private Integer middles;
    private Integer leaves;
    private Integer independents;

    public SelectedGraphTableItem(Integer roots, Integer middles, Integer leaves, Integer independents) {
        this.targets = roots + middles + leaves + independents;
        this.roots = roots;
        this.middles = middles;
        this.leaves = leaves;
        this.independents = independents;
    }

    public Integer getTargets() {
        return this.targets;
    }

    public void setTargets(Integer targets) {
        this.targets = targets;
    }

    public Integer getRoots() {
        return this.roots;
    }

    public void setRoots(Integer roots) {
        this.roots = roots;
    }

    public Integer getMiddles() {
        return this.middles;
    }

    public void setMiddles(Integer middles) {
        this.middles = middles;
    }

    public Integer getLeaves() {
        return this.leaves;
    }

    public void setLeaves(Integer leaves) {
        this.leaves = leaves;
    }

    public Integer getIndependents() {
        return this.independents;
    }

    public void setIndependents(Integer independents) {
        this.independents = independents;
    }
}
