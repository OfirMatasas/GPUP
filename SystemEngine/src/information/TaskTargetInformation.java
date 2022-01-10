package information;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import summaries.TargetSummary;

public class TaskTargetInformation {

        private final SimpleIntegerProperty number;
        private final SimpleStringProperty targetName;
        private final SimpleStringProperty position;
        private final SimpleStringProperty currentRuntimeStatus;
        private final SimpleStringProperty resultStatus;

        public TaskTargetInformation(int number, String targetName, String position, String currentRuntimeStatus, String resultStatus) {
            this.number = new SimpleIntegerProperty(number);
            this.targetName = new SimpleStringProperty(targetName);
            this.position = new SimpleStringProperty(position);
            this.currentRuntimeStatus = new SimpleStringProperty(currentRuntimeStatus);
            this.resultStatus = new SimpleStringProperty(resultStatus);
        }

        public int getNumber() {
            return number.intValue();
        }

        public IntegerProperty numberProperty() {
            return number;
        }

        public void setNumber(int number) {
            this.number.set(number);
        }

        public String getTargetName() {
            return targetName.get();
        }

        public StringProperty targetNameProperty() {
            return targetName;
        }

        public void setTargetName(String targetName) {
            this.targetName.set(targetName);
        }

        public String getPosition() {
            return position.get();
        }

        public StringProperty positionProperty() {
            return position;
        }

        public void setPosition(String position) {
            this.position.set(position);
        }

        public String getCurrentRuntimeStatus() {
            return currentRuntimeStatus.get();
        }

        public StringProperty currentRuntimeStatusProperty() {
            return currentRuntimeStatus;
        }

        public void setCurrentRuntimeStatus(String currentRuntimeStatus) {
            if(currentRuntimeStatus.equals(TargetSummary.RuntimeStatus.InProcess.toString()))
                this.currentRuntimeStatus.set("In process");
            else
                this.currentRuntimeStatus.set(currentRuntimeStatus);
        }
        public String getResultStatus() {return resultStatus.get();}

        public SimpleStringProperty resultStatusProperty() {return resultStatus;}

        public void setResultStatus(String resultStatus) {this.resultStatus.set(resultStatus);}



    }
