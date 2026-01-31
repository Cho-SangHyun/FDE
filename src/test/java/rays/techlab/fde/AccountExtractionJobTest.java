package rays.techlab.fde;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBatchTest
// SpringBatchTest : 배치 테스트에 필요한 유틸리티들을 자동으로 준비
// - JobLauncherTestUtils: Job 실행 및 Step 개별 실행을 위한 유틸리티
// - JobRepositoryTestUtils: Job 실행 이력 생성 및 정리 등 JobRepository 관련 유틸리티
// - StepScopeTestExecutionListener: @StepScope 빈 테스트 지원
// - JobScopeTestExecutionListener: @JobScope 빈 테스트 지원
@SpringBootTest
@ActiveProfiles("test") // 'test' 프로파일 활성화
public class AccountExtractionJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job accountExtractionJob;

    @TempDir // 임시 디렉토리를 자동으로 생성하고 관리해주는 어노테이션
    private Path tempDir;

    @PostConstruct
    public void configureJobLauncherTestUtils() throws Exception {
        jobLauncherTestUtils.setJob(accountExtractionJob);
    }

    @BeforeEach
    void setUp() {
        // 배치가 실행될 때 사용할 추출결과파일 경로를 임시 디렉토리 내부로 세팅
        // @TempDir을 사용하면 테스트가 끝날 때마다 파일이 자동으로 삭제되는 효과도 있다! (임시경로 만드는 거임)
        String testOutputPath = tempDir.resolve("output.txt").toString();
        System.setProperty("app.batch.output-path", testOutputPath);
    }

    @Test
    @DisplayName("계좌정보추출 배치 잡 실행 테스트")
    void accountExtractionJobTest() throws Exception {
        // getUniqueJobParametersBuilder(): 잡 인스턴스 중복 회피를 위해 설정 (랜덤 파라미터 자동 생성)
        JobParameters jobParameters = jobLauncherTestUtils.getUniqueJobParametersBuilder()
                        .addLong("businessUnitId", 1L)
                        .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        Path expectedOutputFile = Paths.get("src/test/resources/output.txt");
        Path actualOutputFile = tempDir.resolve("output.txt");

        List<String> expectedLines = Files.readAllLines(expectedOutputFile, Charset.forName("EUC-KR"));
        List<String> actualLines = Files.readAllLines(actualOutputFile, Charset.forName("EUC-KR"));

        Assertions.assertLinesMatch(expectedLines, actualLines);
    }
}
