package information;

public class TargetDetails {
    private int num;
    private String targetName;
    private String  position;
    private int directDependsOn;
    private int allDependsOn;
    private int directRequiredFor;
    private int allRequiredFor;
    private String extraInformation;

    public TargetDetails(int num, String targetName, String position, int directDependsOn, int allDependsOn,
                         int directRequiredFor, int allRequiredFor, String extraInformation) {
        this.num = num;
        this.targetName = targetName;
        this.position = position;
        this.directDependsOn = directDependsOn;
        this.allDependsOn = allDependsOn;
        this.directRequiredFor = directRequiredFor;
        this.allRequiredFor = allRequiredFor;
        this.extraInformation = extraInformation;
    }

    public int getNum() {
        return this.num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getPosition() {
        return this.position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getDirectDependsOn() {
        return this.directDependsOn;
    }

    public void setDirectDependsOn(int directDependsOn) {
        this.directDependsOn = directDependsOn;
    }

    public int getAllDependsOn() {
        return this.allDependsOn;
    }

    public void setAllDependsOn(int allDependsOn) {
        this.allDependsOn = allDependsOn;
    }

    public int getDirectRequiredFor() {
        return this.directRequiredFor;
    }

    public void setDirectRequiredFor(int directRequiredFor) {
        this.directRequiredFor = directRequiredFor;
    }

    public int getAllRequiredFor() {
        return this.allRequiredFor;
    }

    public void setAllRequiredFor(int allRequiredFor) {
        this.allRequiredFor = allRequiredFor;
    }

    public String getExtraInformation() {return this.extraInformation;}

    public void setExtraInformation(String extraInformation) {this.extraInformation = extraInformation;}
}
