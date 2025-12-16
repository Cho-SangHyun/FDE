package rays.techlab.fde;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FdeApplication {

	public static void main(String[] args) {
		// 배치앱은 SpringApplication.run()의 결과를 System.exit()로 처리하는 것이 권장
		// 배치 작업의 성공/실패 상태를 exit code로 외부 시스템에 전달 -> 실무에서 배치 모니터링과 제어에 필수
		System.exit(SpringApplication.exit(SpringApplication.run(FdeApplication.class, args)));
	}

}
