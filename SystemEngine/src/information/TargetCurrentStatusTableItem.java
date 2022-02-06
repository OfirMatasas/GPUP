package information;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import summaries.TargetSummary;

public class TargetCurrentStatusTableItem {

    private final SimpleIntegerProperty number;
    private final SimpleStringProperty targetName;
    private final SimpleStringProperty currentRuntimeStatus;
    private final SimpleStringProperty resultStatus;

    public TargetCurrentStatusTableItem(int number, String targetName, String currentRuntimeStatus, String resultStatus) {
        this.number = new SimpleIntegerProperty(number);
        this.targetName = new SimpleStringProperty(targetName);
        this.currentRuntimeStatus = new SimpleStringProperty(currentRuntimeStatus);
        this.resultStatus = new SimpleStringProperty(resultStatus);
    }

    public int getNumber() {
        return this.number.intValue();
    }

    public IntegerProperty numberProperty() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number.set(number);
    }

    public String getTargetName() {
        return this.targetName.get();
    }

    public StringProperty targetNameProperty() {
        return this.targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName.set(targetName);
    }

    public String getCurrentRuntimeStatus() {
        return this.currentRuntimeStatus.get();
    }

    public StringProperty currentRuntimeStatusProperty() {
        return this.currentRuntimeStatus;
    }

    public void setCurrentRuntimeStatus(String currentRuntimeStatus) {
        if(currentRuntimeStatus.equals(TargetSummary.RuntimeStatus.InProcess.toString()))
            this.currentRuntimeStatus.set("In process");
        else
            this.currentRuntimeStatus.set(currentRuntimeStatus);
    }
    public String getResultStatus() {return this.resultStatus.get();}

    public SimpleStringProperty resultStatusProperty() {return this.resultStatus;}

    public void setResultStatus(String resultStatus) {this.resultStatus.set(resultStatus);}
}
