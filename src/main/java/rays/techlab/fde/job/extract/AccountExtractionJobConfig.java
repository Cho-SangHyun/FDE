package rays.techlab.fde.job.extract;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import rays.techlab.fde.domain.account.dto.DemandTargetDto;
import rays.techlab.fde.domain.account.dto.ExtractedAccountDto;
import rays.techlab.fde.domain.account.mapper.AccountExtractionMapper;
import rays.techlab.fde.global.support.FixedByteLengthLineAggregator;
import rays.techlab.fde.job.extract.dto.AccountInformationDemandItem;
import rays.techlab.fde.global.support.FixedByteLengthTokenizer;
import rays.techlab.fde.job.extract.dto.AccountInformationResultItem;

import java.util.HashMap;
import java.util.Map;

/**
 * 계좌정보추출 배치 잡 설정 클래스
 */
@Configuration
public class AccountExtractionJobConfig {

    private final SqlSessionFactory sqlSessionFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountExtractionMapper accountExtractionMapper;

    private final int CHUNK_SIZE = 100;

    public AccountExtractionJobConfig(
            SqlSessionFactory sqlSessionFactory,
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            AccountExtractionMapper accountExtractionMapper
    ) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.accountExtractionMapper = accountExtractionMapper;
    }

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

    @Bean
    public Step fetchDemandFilePathStep() {
        return new StepBuilder("fetchDemandFilePathStep", jobRepository)
                .tasklet(new DemandFilePathFetcherTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step processDemandFileStep(
            MultiResourceItemReader<AccountInformationDemandItem> reader,
            InhabitantNumberEncryptProcessor processor,
            MyBatisBatchItemWriter<DemandTargetDto> writer
    ) {
        return new StepBuilder("processStep", jobRepository)
                .<AccountInformationDemandItem, DemandTargetDto>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Step extractAccountInformationStep(
            AccountInformationExtractionTasklet accountInformationExtractionTasklet
    ) {
        return new StepBuilder("extractAccountStep", jobRepository)
                .tasklet(accountInformationExtractionTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public MultiResourceItemReader<AccountInformationDemandItem> multiDemandFileReader(
            @Value("#{jobExecutionContext['demandFilePaths']}")
            String demandFilePaths
    ) {

        String[] demandFilePathArray = demandFilePaths.split(",");
        Resource[] demandFileResources = new Resource[demandFilePathArray.length];
        for (int i = 0; i < demandFilePathArray.length; i++) {
            demandFileResources[i] = new FileSystemResource(demandFilePathArray[i]);
        }

        return new MultiResourceItemReaderBuilder<AccountInformationDemandItem>()
                .name("multiDemandFileReader")
                .resources(demandFileResources)
                .delegate(accountInformationDemandReader())
                .build();
    }

    @Bean
    public FlatFileItemReader<AccountInformationDemandItem> accountInformationDemandReader() {
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
        BeanWrapperFieldSetMapper<AccountInformationDemandItem> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(AccountInformationDemandItem.class);

        // LineMapper 생성 (Tokenizer + FieldSetMapper 연결)
        DefaultLineMapper<AccountInformationDemandItem> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return new FlatFileItemReaderBuilder<AccountInformationDemandItem>()
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
                .statementId(AccountExtractionMapper.class.getName() + ".insertDemandTarget")
                .assertUpdates(false)
                .build();
    }

    @Bean
    @StepScope
    public AccountInformationExtractionTasklet accountInformationExtractionTasklet(
            @Value("#{jobParameters['businessUnitId']}")
            Long businessUnitId
    ) {
        return new AccountInformationExtractionTasklet(accountExtractionMapper, businessUnitId);
    }

    @Bean
    public Step writeResultFileStep(
            MyBatisCursorItemReader<ExtractedAccountDto> reader,
            DecryptProcessor processor,
            FlatFileItemWriter<AccountInformationResultItem> writer
    ) {
        return new StepBuilder("writeResultFileStep", jobRepository)
                .<ExtractedAccountDto, AccountInformationResultItem>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public MyBatisCursorItemReader<ExtractedAccountDto> accountInformationResultItemReader(
            @Value("#{jobParameters['businessUnitId']}")
            Long businessUnitId
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("businessUnitId", businessUnitId);

        return new MyBatisCursorItemReaderBuilder<ExtractedAccountDto>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId(AccountExtractionMapper.class.getName() + ".selectExtractedAccountInformation")
                .parameterValues(params)
                .build();
    }

    @Bean
    @StepScope
    public DecryptProcessor decryptProcessor() {
        return new DecryptProcessor();
    }

    // Writer는 FieldExtractor로 객체에서 값들을 뽑아내고 LineAggregator로 한 줄의 문자열로 만들어 파일에 기록
    // 정확히는 LineAggregator가 FieldExtractor를 사용 (멤버변수로 사용한다는 의미)
    // FieldExtractor는 대표적으론 두 가지가 존재
    // 1) BeanWrapperFieldExtractor: Java Bean 객체로부터 getter 메서드를 사용하여 필드 값을 추출
    //     - 자바 빈 객체란 : 스프링 빈이랑 다른 개념. 특정 규칙을 지켜 작성된 Reusable한 클래스를 말함
    //     - 기본 생성자 존재, 모든 필드는 private, getter / setter를 통해 접근, 직렬화가 구현된 클래스!
    // 2) RecordFieldExtractor: 레코드 타입(Java Record)에서 필드 값을 추출
    @Bean
    @StepScope
    public FlatFileItemWriter<AccountInformationResultItem> accountInformationResultItemWriter() {
        return new FlatFileItemWriterBuilder<AccountInformationResultItem>()
                .name("accountInformationResultItemWriter")
                .encoding("EUC-KR")
                .resource(new FileSystemResource("src/main/resources/output.txt"))
                .lineAggregator(new FixedByteLengthLineAggregator<AccountInformationResultItem>(
                        (item) -> new Object[] {
                                item.sequenceNumber(),
                                item.custName(),
                                item.inhabitantNumber(),
                                item.accountNumber(),
                                item.productType(),
                                item.productName(),
                                item.balance()
                        },
                        new int[] {8, 30, 14, 30, 2, 20, 20}
                ))
                .build();
    }


}
