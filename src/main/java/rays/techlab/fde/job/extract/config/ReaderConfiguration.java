package rays.techlab.fde.job.extract.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.builder.MyBatisCursorItemReaderBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import rays.techlab.fde.domain.account.dto.ExtractedAccountDto;
import rays.techlab.fde.domain.account.mapper.AccountExtractionMapper;
import rays.techlab.fde.global.support.FixedByteLengthTokenizer;
import rays.techlab.fde.job.extract.dto.AccountInformationDemandItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Reader 설정 클래스
 *
 * 배치 잡의 Reader 빈들을 관리합니다.
 */
@Configuration
public class ReaderConfiguration {

    private final SqlSessionFactory sqlSessionFactory;

    public ReaderConfiguration(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * 여러 요구 대상자 파일을 읽는 MultiResourceItemReader
     */
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

    /**
     * 요구 대상자 파일을 읽는 FlatFileItemReader
     */
    @Bean
    public FlatFileItemReader<AccountInformationDemandItem> accountInformationDemandReader() {
        // 커스텀 Tokenizer 생성 및 설정
        FixedByteLengthTokenizer tokenizer = new FixedByteLengthTokenizer();
        tokenizer.setRanges(FileFormatConfiguration.DemandFileFormat.getRanges());
        tokenizer.setNames(FileFormatConfiguration.DemandFileFormat.FIELD_NAMES);

        // BeanWrapperFieldSetMapper: 토큰화된 결과를 자바 객체에 매핑
        BeanWrapperFieldSetMapper<AccountInformationDemandItem> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(AccountInformationDemandItem.class);

        // LineMapper 생성 (Tokenizer + FieldSetMapper 연결)
        DefaultLineMapper<AccountInformationDemandItem> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return new FlatFileItemReaderBuilder<AccountInformationDemandItem>()
                .name("accountInformationDemandReader")
                .encoding(FileFormatConfiguration.DEFAULT_ENCODING)
                .lineMapper(lineMapper)
                .build();
    }

    /**
     * 추출된 계좌 정보를 DB에서 읽는 MyBatisCursorItemReader
     */
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
}