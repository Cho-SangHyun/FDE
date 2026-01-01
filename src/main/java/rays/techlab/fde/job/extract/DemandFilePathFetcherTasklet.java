package rays.techlab.fde.job.extract;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;

/**
 * 요구 대상자 파일 경로 조회 Tasklet
 */
public class DemandFilePathFetcherTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // DB에서 요구 대상자 파일들의 경로들을 조회해왔다고 가정
        List<String> demandFilePaths = List.of(
                "src/main/resources/testfile.txt"
        );

        if (demandFilePaths.isEmpty()) {
            throw new RuntimeException("처리할 파일들의 정보가 없습니다");
        }

        String demandFilePathString = String.join(",", demandFilePaths);

        // JobExecution의 ExecutionContext에 파일 경로들 저장
        chunkContext.getStepContext().getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("demandFilePaths", demandFilePathString);

        return RepeatStatus.FINISHED;
    }
}
