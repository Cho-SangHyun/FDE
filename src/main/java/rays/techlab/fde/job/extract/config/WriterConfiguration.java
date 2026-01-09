package rays.techlab.fde.job.extract.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import rays.techlab.fde.domain.account.dto.DemandTargetDto;
import rays.techlab.fde.domain.account.mapper.AccountExtractionMapper;
import rays.techlab.fde.global.support.FixedByteLengthLineAggregator;
import rays.techlab.fde.job.extract.dto.AccountInformationResultItem;

/**
 * Writer 설정 클래스
 *
 * 배치 잡의 Writer 빈들을 관리합니다.
 */
@Configuration
public class WriterConfiguration {

    private final SqlSessionFactory sqlSessionFactory;

    public WriterConfiguration(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * 요구 대상자 정보를 DB에 저장하는 MyBatisBatchItemWriter
     */
    @Bean
    @StepScope
    public MyBatisBatchItemWriter<DemandTargetDto> accountInformationDemandItemWriter() {
        return new MyBatisBatchItemWriterBuilder<DemandTargetDto>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId(AccountExtractionMapper.class.getName() + ".insertDemandTarget")
                .assertUpdates(false)
                .build();
    }

    /**
     * 계좌 정보 추출 결과를 파일로 저장하는 FlatFileItemWriter
     */
    @Bean
    @StepScope
    public FlatFileItemWriter<AccountInformationResultItem> accountInformationResultItemWriter() {
        return new FlatFileItemWriterBuilder<AccountInformationResultItem>()
                .name("accountInformationResultItemWriter")
                .encoding(FileFormatConfiguration.DEFAULT_ENCODING)
                .resource(new FileSystemResource("src/main/resources/output.txt"))
                .lineAggregator(new FixedByteLengthLineAggregator<>(
                        (item) -> new Object[] {
                                item.sequenceNumber(),
                                item.custName(),
                                item.inhabitantNumber(),
                                item.accountNumber(),
                                item.productType(),
                                item.productName(),
                                item.balance()
                        },
                        FileFormatConfiguration.ResultFileFormat.getFieldLengths()
                ))
                .build();
    }
}