package rays.techlab.fde.job.extract;

import org.springframework.batch.item.ItemProcessor;
import rays.techlab.fde.domain.account.dto.DemandTargetDto;
import rays.techlab.fde.global.support.AESUtil;
import rays.techlab.fde.job.extract.dto.AccountInformationDemandItem;

/**
 * 주민등록번호 암호화 Processor
 */
public class InhabitantNumberEncryptProcessor implements ItemProcessor<AccountInformationDemandItem, DemandTargetDto> {

    private final Long businessUnitId;

    public InhabitantNumberEncryptProcessor(Long businessUnitId) {
        this.businessUnitId = businessUnitId;
    }

    @Override
    public DemandTargetDto process(AccountInformationDemandItem item) throws Exception {
        String encryptedInhabitantNumber = AESUtil.encrypt(item.getInhabitantNumber());
        Long sequenceNumber = Long.parseLong(item.getSequenceNumber());

        return new DemandTargetDto(
                businessUnitId,
                sequenceNumber,
                encryptedInhabitantNumber,
                item.getTargetName(),
                item.getBaseDate()
        );
    }
}
