package rays.techlab.fde.job.extract.dto;

import io.github.Cho_SangHyun.fixedbyte.annotation.Align;
import io.github.Cho_SangHyun.fixedbyte.annotation.FixedByteField;
import io.github.Cho_SangHyun.fixedbyte.annotation.FixedByteRecord;

@FixedByteRecord(encoding = "EUC-KR")
public class AccountInformationDemandItem {

    @FixedByteField(order = 1, length = 8, align = Align.RIGHT, padChar = '0')
    private String sequenceNumber;

    @FixedByteField(order = 2, length = 14, align = Align.LEFT)
    private String inhabitantNumber;

    @FixedByteField(order = 3, length = 30, align = Align.LEFT)
    private String targetName;

    @FixedByteField(order = 4, length = 8, align = Align.LEFT)
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

