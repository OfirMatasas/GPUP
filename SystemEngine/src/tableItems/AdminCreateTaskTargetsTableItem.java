package tableItems;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AdminCreateTaskTargetsTableItem
{
    private final SimpleIntegerProperty number;
    private final SimpleStringProperty targetName;
    private final SimpleStringProperty position;

    public AdminCreateTaskTargetsTableItem(int number, String targetName, String position) {
        this.number = new SimpleIntegerProperty(number);
        this.targetName = new SimpleStringProperty(targetName);
        this.position = new SimpleStringProperty(position);
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

    public String getPosition() {
        return this.position.get();
    }

    public StringProperty positionProperty() {
        return this.position;
    }

    public void setPosition(String position) {
        this.position.set(position);
    }
}