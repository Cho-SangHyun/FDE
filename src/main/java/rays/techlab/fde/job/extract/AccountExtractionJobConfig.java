package rays.techlab.fde.job.extract;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import rays.techlab.fde.job.extract.dto.AccountInformationDemand;
import rays.techlab.fde.global.support.FixedByteLengthTokenizer;

@Configuration
public class AccountExtractionJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public AccountExtractionJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job accountExtractionJob() {
        return new JobBuilder("accountExtractionJob", jobRepository)
                .start(processDemandFileStep(accountInformationDemandReader(), accountInformationDemandItemWriter()))
                .build();
    }

    @Bean
    public Step processDemandFileStep(
            FlatFileItemReader<AccountInformationDemand> reader,
            AccountInformationDemandItemWriter writer
    ) {
        return new StepBuilder("processStep", jobRepository)
                .<AccountInformationDemand, AccountInformationDemand>chunk(10, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<AccountInformationDemand> accountInformationDemandReader() {
        // 커스텀 Tokenizer 생성 및 설정
        FixedByteLengthTokenizer tokenizer = new FixedByteLengthTokenizer();
        tokenizer.setEncoding("EUC-KR");
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
                .resource(new FileSystemResource("src/main/resources/testfile.txt"))
                .encoding("EUC-KR")
                .lineMapper(lineMapper)
                .build();
    }

    @Bean
    public AccountInformationDemandItemWriter accountInformationDemandItemWriter() {
        return new AccountInformationDemandItemWriter();
    }

    public static class AccountInformationDemandItemWriter implements ItemWriter<AccountInformationDemand> {
        @Override
        public void write(Chunk<? extends AccountInformationDemand> chunk) throws Exception {
            for (AccountInformationDemand item : chunk) {
                System.out.println("처리 대상 계좌 정보: " + item.toString());
            }
        }
    }

}
