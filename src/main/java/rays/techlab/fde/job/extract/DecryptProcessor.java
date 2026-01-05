package rays.techlab.fde.job.extract;

import org.springframework.batch.item.ItemProcessor;
import rays.techlab.fde.domain.account.dto.ExtractedAccountDto;
import rays.techlab.fde.global.support.AESUtil;
import rays.techlab.fde.job.extract.dto.AccountInformationResultItem;

/**
 * 주민번호, 계좌번호 복호화 Processor
 */
public class DecryptProcessor implements ItemProcessor<ExtractedAccountDto, AccountInformationResultItem> {

    @Override
    public AccountInformationResultItem process(ExtractedAccountDto item) throws Exception {
        String decryptedInhabitantNumber = AESUtil.decrypt(item.inhabitantNumber());
        String decryptedAccountNumber = AESUtil.decrypt(item.accountNumber());

        return new AccountInformationResultItem(
                item.sequenceNumber(),
                item.custName(),
                decryptedInhabitantNumber,
                decryptedAccountNumber,
                item.productName(),
                item.productType(),
                item.balance()
        );
    }
}
