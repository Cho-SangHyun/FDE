package rays.techlab.fde.job.extract.dto;

public class AccountInformationDemand {

    private String sequenceNumber;
    private String inhabitantNumber;
    private String targetName;
    private String baseDate;

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getInhabitantNumber() {
        return inhabitantNumber;
    }

    public void setInhabitantNumber(String inhabitantNumber) {
        this.inhabitantNumber = inhabitantNumber;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(String baseDate) {
        this.baseDate = baseDate;
    }

    public String toString() {
        return "{" +
                "sequenceNumber='" + sequenceNumber + '\'' +
                ", inhabitantNumber='" + inhabitantNumber + '\'' +
                ", targetName='" + targetName + '\'' +
                ", baseDate='" + baseDate + '\'' +
                '}';
    }
}

