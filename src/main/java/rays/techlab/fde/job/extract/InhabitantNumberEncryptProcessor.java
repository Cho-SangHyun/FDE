package rays.techlab.fde.job.extract;

import org.springframework.batch.item.ItemProcessor;
import rays.techlab.fde.global.support.AESUtil;
import rays.techlab.fde.job.extract.dto.AccountInformationDemand;

public class InhabitantNumberEncryptProcessor implements ItemProcessor<AccountInformationDemand, AccountInformationDemand> {

    @Override
    public AccountInformationDemand process(AccountInformationDemand item) throws Exception {
        String encryptedInhabitantNumber = AESUtil.encrypt(item.getInhabitantNumber());
        item.setInhabitantNumber(encryptedInhabitantNumber);
        return item;
    }
}
