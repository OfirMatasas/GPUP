package information;

public class TargetDetails {
    private int num;
    private String targetName;
    private String  position;
    private int directDependsOn;
    private int allDependsOn;
    private int directRequiredFor;
    private int allRequiredFor;
    private int serialSets;
    private String extraInformation;

    public TargetDetails(int num, String targetName, String position, int directDependsOn, int allDependsOn, int directRequiredFor, int allRequiredFor, int serialSets,String extraInformation) {
        this.num = num;
        this.targetName = targetName;
        this.position = position;
        this.directDependsOn = directDependsOn;
        this.allDependsOn = allDependsOn;
        this.directRequiredFor = directRequiredFor;
        this.allRequiredFor = allRequiredFor;
        this.serialSets = serialSets;
        this.extraInformation = extraInformation;

    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num=num;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName=targetName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position=position;
    }

    public int getDirectDependsOn() {
        return directDependsOn;
    }

    public void setDirectDependsOn(int directDependsOn) {
        this.directDependsOn=directDependsOn;
    }

    public int getAllDependsOn() {
        return allDependsOn;
    }

    public void setAllDependsOn(int allDependsOn) {
        this.allDependsOn=allDependsOn;
    }

    public int getDirectRequiredFor() {
        return directRequiredFor;
    }

    public void setDirectRequiredFor(int directRequiredFor) {
        this.directRequiredFor=directRequiredFor;
    }

    public int getAllRequiredFor() {
        return allRequiredFor;
    }

    public void setAllRequiredFor(int allRequiredFor) {
        this.allRequiredFor=allRequiredFor;
    }

    public int getSerialSets() {
        return serialSets;
    }

    public void setSerialSets(int serialSets) {
        this.serialSets=serialSets;
    }

    public String getExtraInformation() {return extraInformation;}

    public void setExtraInformation(String extraInformation) {this.extraInformation = extraInformation;}


}
