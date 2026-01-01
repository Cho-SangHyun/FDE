package rays.techlab.fde.job.extract;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import rays.techlab.fde.domain.account.dto.ExtractionCriteriaDto;
import rays.techlab.fde.domain.account.mapper.AccountExtractionMapper;

public class AccountInformationExtractionTasklet implements Tasklet {

    private final AccountExtractionMapper accountExtractionMapper;
    private final Long businessUnitId;

    public AccountInformationExtractionTasklet(
            AccountExtractionMapper accountExtractionMapper,
            Long businessUnitId
    ) {
        this.accountExtractionMapper = accountExtractionMapper;
        this.businessUnitId = businessUnitId;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // 조회기준일(baseDate) 등은 실제 운영 시점에서는 파라미터 등으로 주입받아야 함
        ExtractionCriteriaDto criteriaDto = new ExtractionCriteriaDto(
                businessUnitId,
                "2025-12-15",
                "2025-12-01",
                "2025-12-29"
        );

        accountExtractionMapper.extractAccountInformation(criteriaDto);

        return RepeatStatus.FINISHED;
    }
}
