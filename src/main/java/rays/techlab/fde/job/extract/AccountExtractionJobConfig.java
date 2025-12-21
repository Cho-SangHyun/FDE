package rays.techlab.fde.job.extract;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import rays.techlab.fde.domain.account.dto.DemandTargetDto;
import rays.techlab.fde.domain.account.mapper.DemandTargetMapper;
import rays.techlab.fde.job.extract.dto.AccountInformationDemand;
import rays.techlab.fde.global.support.FixedByteLengthTokenizer;

/**
 * 계좌정보추출 배치 잡 설정 클래스
 */
@Configuration
public class AccountExtractionJobConfig {

    private final SqlSessionFactory sqlSessionFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final int CHUNK_SIZE = 100;

    public AccountExtractionJobConfig(
            @Autowired SqlSessionFactory sqlSessionFactory,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job accountExtractionJob(
            Step fetchDemandFilePathStep,
            Step processDemandFileStep
    ) {
        return new JobBuilder("accountExtractionJob", jobRepository)
                .start(fetchDemandFilePathStep)
                .next(processDemandFileStep)
                .build();
    }

    @Bean
    public Step fetchDemandFilePathStep() {
        return new StepBuilder("fetchDemandFilePathStep", jobRepository)
                .tasklet(new DemandFilePathFetcherTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step processDemandFileStep(
            MultiResourceItemReader<AccountInformationDemand> reader,
            InhabitantNumberEncryptProcessor processor,
            MyBatisBatchItemWriter<DemandTargetDto> writer
    ) {
        return new StepBuilder("processStep", jobRepository)
                .<AccountInformationDemand, DemandTargetDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public MultiResourceItemReader<AccountInformationDemand> multiDemandFileReader(
            @Value("#{jobExecutionContext['demandFilePaths']}") String demandFilePaths
    ) {

        String[] demandFilePathArray = demandFilePaths.split(",");
        Resource[] demandFileResources = new Resource[demandFilePathArray.length];
        for (int i = 0; i < demandFilePathArray.length; i++) {
            demandFileResources[i] = new FileSystemResource(demandFilePathArray[i]);
        }

        return new MultiResourceItemReaderBuilder<AccountInformationDemand>()
                .name("multiDemandFileReader")
                .resources(demandFileResources)
                .delegate(accountInformationDemandReader())
                .build();
    }

    @Bean
    public FlatFileItemReader<AccountInformationDemand> accountInformationDemandReader() {
        // 커스텀 Tokenizer 생성 및 설정
        FixedByteLengthTokenizer tokenizer = new FixedByteLengthTokenizer();
        tokenizer.setRanges(new Range[] {
                new Range(1, 8),     // 일련번호
                new Range(9, 22),    // 주민번호
                new Range(23, 52),   // 대상자명
                new Range(53, 61)    // 조회기준일
        });
        tokenizer.setNames("sequenceNumber", "inhabitantNumber", "targetName", "baseDate");

        // BeanWrapperFieldSetMapper: 토큰화된 결과를 자바 객체에 매핑
        BeanWrapperFieldSetMapper<AccountInformationDemand> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(AccountInformationDemand.class);

        // LineMapper 생성 (Tokenizer + FieldSetMapper 연결)
        DefaultLineMapper<AccountInformationDemand> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return new FlatFileItemReaderBuilder<AccountInformationDemand>()
                .name("accountInformationDemandReader")
                .encoding("EUC-KR")
                .lineMapper(lineMapper)
                .build();
    }

    @Bean
    @StepScope
    public InhabitantNumberEncryptProcessor inhabitantNumberEncryptProcessor(
            @Value("#{jobParameters['businessUnitId']}") Long businessUnitId
    ) {
        return new InhabitantNumberEncryptProcessor(businessUnitId);
    }

    @Bean
    @StepScope
    public MyBatisBatchItemWriter<DemandTargetDto> accountInformationDemandItemWriter() {
        return new MyBatisBatchItemWriterBuilder<DemandTargetDto>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId(DemandTargetMapper.class.getName() + ".insertDemandTarget")
                .assertUpdates(false)
                .build();
    }


}
