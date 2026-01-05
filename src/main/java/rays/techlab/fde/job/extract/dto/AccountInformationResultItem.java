package rays.techlab.fde.job.extract.dto;

import java.math.BigDecimal;

public record AccountInformationResultItem(

        String sequenceNumber,
        String custName,
        String inhabitantNumber,
        String accountNumber,
        String productName,
        String productType,
        BigDecimal balance
) {
}
