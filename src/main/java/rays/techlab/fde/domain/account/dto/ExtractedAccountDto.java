package rays.techlab.fde.domain.account.dto;

import java.math.BigDecimal;

public record ExtractedAccountDto(
        Long businessUnitId,
        String sequenceNumber,
        Long custId,
        String custName,
        String inhabitantNumber,
        String accountNumber,
        String productName,
        String productType,
        BigDecimal balance
) {
}
