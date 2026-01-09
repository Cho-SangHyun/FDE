package rays.techlab.fde.job.extract;

import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import rays.techlab.fde.domain.account.dto.DemandTargetDto;
import rays.techlab.fde.domain.account.dto.ExtractedAccountDto;
import rays.techlab.fde.domain.account.mapper.AccountExtractionMapper;
import rays.techlab.fde.job.extract.dto.AccountInformationDemandItem;
import rays.techlab.fde.job.extract.dto.AccountInformationResultItem;

/**
 * 계좌정보추출 배치 잡 설정 클래스
 *
 * Job과 Step의 구성을 정의합니다.
 * Reader, Writer, Processor는 각각의 Configuration 클래스에서 관리됩니다.
 */
@Configuration
public class AccountExtractionJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountExtractionMapper accountExtractionMapper;

    private static final int CHUNK_SIZE = 100;

    public AccountExtractionJobConfig(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            AccountExtractionMapper accountExtractionMapper
    ) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.accountExtractionMapper = accountExtractionMapper;
    }

    /**
     * 계좌 정보 추출 Job
     *
     * 1. fetchDemandFilePathStep: 처리할 파일 경로 조회
     * 2. processDemandFileStep: 파일 읽고 주민번호 암호화 후 DB 저장
     * 3. extractAccountInformationStep: 계좌 정보 추출
     * 4. writeResultFileStep: 추출 결과를 파일로 저장
     */
    @Bean
    public Job accountExtractionJob(
            Step fetchDemandFilePathStep,
            Step processDemandFileStep,
            Step extractAccountInformationStep,
            Step writeResultFileStep
    ) {
        return new JobBuilder("accountExtractionJob", jobRepository)
                .start(fetchDemandFilePathStep)
                .next(processDemandFileStep)
                .next(extractAccountInformationStep)
                .next(writeResultFileStep)
                .build();
    }

    /**
     * Step 1: 처리할 요구 대상자 파일 경로 조회
     */
    @Bean
    public Step fetchDemandFilePathStep() {
        return new StepBuilder("fetchDemandFilePathStep", jobRepository)
                .tasklet(new DemandFilePathFetcherTasklet(), transactionManager)
                .build();
    }

    /**
     * Step 2: 요구 대상자 파일 읽고 주민번호 암호화 후 DB 저장
     */
    @Bean
    public Step processDemandFileStep(
            MultiResourceItemReader<AccountInformationDemandItem> multiDemandFileReader,
            InhabitantNumberEncryptProcessor inhabitantNumberEncryptProcessor,
            MyBatisBatchItemWriter<DemandTargetDto> accountInformationDemandItemWriter
    ) {
        return new StepBuilder("processDemandFileStep", jobRepository)
                .<AccountInformationDemandItem, DemandTargetDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(multiDemandFileReader)
                .processor(inhabitantNumberEncryptProcessor)
                .writer(accountInformationDemandItemWriter)
                .build();
    }

    /**
     * Step 3: 계좌 정보 추출
     */
    @Bean
    public Step extractAccountInformationStep(
            AccountInformationExtractionTasklet accountInformationExtractionTasklet
    ) {
        return new StepBuilder("extractAccountInformationStep", jobRepository)
                .tasklet(accountInformationExtractionTasklet, transactionManager)
                .build();
    }

    /**
     * Step 4: 추출된 계좌 정보를 파일로 저장
     */
    @Bean
    public Step writeResultFileStep(
            MyBatisCursorItemReader<ExtractedAccountDto> accountInformationResultItemReader,
            DecryptProcessor decryptProcessor,
            FlatFileItemWriter<AccountInformationResultItem> accountInformationResultItemWriter
    ) {
        return new StepBuilder("writeResultFileStep", jobRepository)
                .<ExtractedAccountDto, AccountInformationResultItem>chunk(CHUNK_SIZE, transactionManager)
                .reader(accountInformationResultItemReader)
                .processor(decryptProcessor)
                .writer(accountInformationResultItemWriter)
                .build();
    }

    /**
     * 계좌 정보 추출 Tasklet
     */
    @Bean
    @StepScope
    public AccountInformationExtractionTasklet accountInformationExtractionTasklet(
            @Value("#{jobParameters['businessUnitId']}")
            Long businessUnitId
    ) {
        return new AccountInformationExtractionTasklet(accountExtractionMapper, businessUnitId);
    }
}