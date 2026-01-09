package rays.techlab.fde.job.extract.config;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rays.techlab.fde.job.extract.DecryptProcessor;
import rays.techlab.fde.job.extract.InhabitantNumberEncryptProcessor;

/**
 * Processor 설정 클래스
 *
 * 배치 잡의 Processor 빈들을 관리합니다.
 */
@Configuration
public class ProcessorConfiguration {

    /**
     * 주민번호 암호화 Processor
     */
    @Bean
    @StepScope
    public InhabitantNumberEncryptProcessor inhabitantNumberEncryptProcessor(
            @Value("#{jobParameters['businessUnitId']}") Long businessUnitId
    ) {
        return new InhabitantNumberEncryptProcessor(businessUnitId);
    }

    /**
     * 복호화 Processor
     */
    @Bean
    @StepScope
    public DecryptProcessor decryptProcessor() {
        return new DecryptProcessor();
    }
}