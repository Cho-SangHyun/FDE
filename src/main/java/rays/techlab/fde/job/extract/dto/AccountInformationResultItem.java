package rays.techlab.fde.job.extract.dto;

import io.github.Cho_SangHyun.fixedbyte.annotation.Align;
import io.github.Cho_SangHyun.fixedbyte.annotation.FixedByteField;
import io.github.Cho_SangHyun.fixedbyte.annotation.FixedByteRecord;

import java.math.BigDecimal;

@FixedByteRecord(encoding = "EUC-KR")
public record AccountInformationResultItem(
        @FixedByteField(order = 1, length = 8, align = Align.RIGHT, padChar = '0')
        String sequenceNumber,
        @FixedByteField(order = 2, length = 30, align = Align.LEFT)
        String custName,
        @FixedByteField(order = 3, length = 14, align = Align.LEFT)
        String inhabitantNumber,
        @FixedByteField(order = 4, length = 30, align = Align.LEFT)
        String accountNumber,
        @FixedByteField(order = 6, length = 20, align = Align.LEFT)
        String productName,
        @FixedByteField(order = 5, length = 2, align = Align.LEFT)
        String productType,
        @FixedByteField(order = 7, length = 20, align = Align.LEFT)
        BigDecimal balance
) {
}
